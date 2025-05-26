package com.nabto.webrtc.util;

import androidx.annotation.Nullable;

import com.nabto.webrtc.SignalingError;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.util.UUID;

// @TODO: Rename to JWTMessageSigner
// Look at typescript to compare
public class JWTMessageSigner implements MessageSigner {
    private final Key key;
    private final String keyId;

    private int nextMessageSignSeq = 0;
    private int nextMessageVerifySeq = 0;
    private String nonce = UUID.randomUUID().toString();
    @Nullable private String remoteNonce = null;

    public JWTMessageSigner(String sharedSecret, String keyId) {
        this.key = new HmacKey(sharedSecret.getBytes());
        this.keyId = keyId;
    }

    @Override
    public JSONObject signMessage(JSONObject message) {
        if (nextMessageSignSeq != 0 && remoteNonce == null) {
            throw new RuntimeException("Cannot sign the message with sequence number > 1, as we have not yet received a valid message from the remote peer.");
        }

        int seq = nextMessageSignSeq;
        nextMessageSignSeq++;

        JwtClaims claims = new JwtClaims();
        claims.setClaim("message", message);
        claims.setClaim("messageSeq", seq);
        claims.setClaim("signerNonce", nonce);
        claims.setClaim("verifierNonce", remoteNonce);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        jws.setHeader("kid", keyId);
        jws.setKey(key);
        jws.setDoKeyValidation(false);

        try {
            return new JSONObject(jws.getCompactSerialization());
        } catch (JoseException e) {
            throw new IllegalArgumentException("SharedSecretMessageSigner could not sign message " + message);
        } catch (JSONException e) {
            // @TODO: Better description
            throw new RuntimeException("JWTMessageSigner failed to sign message");
        }
    }

    @Override
    public JSONObject verifyMessage(JSONObject token) {
        // @TODO: Proper verification
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setVerificationKey(key)
                .setRelaxVerificationKeyValidation()
                .setJwsAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT, AlgorithmIdentifiers.HMAC_SHA256))
                .build();

        try {
            JwtClaims claims = jwtConsumer.processToClaims(token.toString());
            return new JSONObject(claims.getClaimValueAsString("message"));
        } catch (InvalidJwtException e) {
            throw new SignalingError(SignalingError.VERIFICATION_ERROR, "Cannot verify the JWT token: " + e.getMessage());
        } catch (JSONException e) {
            // @TODO: Better description
            throw new RuntimeException("JWTMessageSigner failed to verify message");
        }
    }
}
