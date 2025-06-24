package com.nabto.webrtc.util;

import androidx.annotation.Nullable;

import com.nabto.webrtc.SignalingError;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class JWTMessageSigner implements MessageSigner {
    private final Key key;
    private final Optional<String> keyId;

    private int nextMessageSignSeq = 0;
    private int nextMessageVerifySeq = 0;
    private String nonce = UUID.randomUUID().toString();
    @Nullable private String remoteNonce = null;

    public JWTMessageSigner(String sharedSecret, Optional<String> keyId) {
        this.key = new HmacKey(sharedSecret.getBytes());
        this.keyId = keyId;
    }

    @Override
    public synchronized JSONObject signMessage(JSONObject message) {
        if (nextMessageSignSeq != 0 && remoteNonce == null) {
            throw new RuntimeException("Cannot sign the message with sequence number > 1, as we have not yet received a valid message from the remote peer.");
        }

        int seq = nextMessageSignSeq;
        nextMessageSignSeq++;

        JSONObject claims = new JSONObject();
        try {
            claims.put("message", message);
            claims.put("messageSeq", seq);
            claims.put("signerNonce", nonce);
            if (remoteNonce != null) {
                claims.put("verifierNonce", remoteNonce);
            }
        } catch (JSONException e) {
            // Should never happen
        }

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toString());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        if (this.keyId.isPresent()) {
            jws.setHeader("kid", keyId.get());
        }
        jws.setKey(key);
        jws.setDoKeyValidation(false);

        try {
            var obj = new JSONObject();
            obj.put("type", "JWT");
            obj.put("jwt", jws.getCompactSerialization());
            return obj;
        } catch (JoseException e) {
            throw new IllegalArgumentException("SharedSecretMessageSigner could not sign message " + message);
        } catch (JSONException e) {
            // @TODO: Better description
            throw new RuntimeException("JWTMessageSigner failed to sign message");
        }
    }

    @Override
    public synchronized JSONObject verifyMessage(JSONObject token) {
        // TODO validate that this is a message with type JWT.
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setVerificationKey(key)
                .setRelaxVerificationKeyValidation()
                .setJwsAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT, AlgorithmIdentifiers.HMAC_SHA256))
                .build();

        try {
            JwtClaims claims = jwtConsumer.processToClaims(token.get("jwt").toString());
            long messageSeq = claims.getClaimValue("messageSeq", Long.class);
            if (messageSeq != nextMessageVerifySeq) {
                throw new SignalingError(SignalingError.VERIFICATION_ERROR, "The message sequence number does not match the expected sequence number.");
            }

            var signerNonce = claims.getClaimValueAsString("signerNonce");
            var verifierNonce = claims.getClaimValueAsString("verifierNonce");
            if (messageSeq == 0) {
                remoteNonce = signerNonce;
            } else {
                if (!Objects.equals(remoteNonce, signerNonce)) {
                    throw new SignalingError(SignalingError.VERIFICATION_ERROR, "The value of messageSignerNonce does not match the expected value for the session.");
                }

                if (!Objects.equals(nonce, verifierNonce)) {
                    throw new SignalingError(SignalingError.VERIFICATION_ERROR, "The value of messageVerifierNonce does not match the expected value for the session.");
                }
            }

            nextMessageVerifySeq++;
            var claimsJson = new JSONObject(claims.toJson());
            return claimsJson.getJSONObject("message");
        } catch (InvalidJwtException e) {
            throw new SignalingError(SignalingError.VERIFICATION_ERROR, "Cannot verify the JWT token: " + e.getMessage());
        } catch (JSONException e) {
            throw new SignalingError(SignalingError.DECODE_ERROR, "Cannot verify the JWT token due to a JSON error: " + e.getMessage());
        } catch (MalformedClaimException e) {
            throw new SignalingError(SignalingError.DECODE_ERROR, "Cannot verify the JWT token due to malformed claims: " + e.getMessage());
        }
    }
}
