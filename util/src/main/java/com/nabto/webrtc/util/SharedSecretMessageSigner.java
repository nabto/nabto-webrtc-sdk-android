package com.nabto.webrtc.util;

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

import java.security.Key;

public class SharedSecretMessageSigner implements MessageSigner {
    private final Key key;
    private final String keyId;
    private int signSeq = 0;

    public SharedSecretMessageSigner(String sharedSecret, String keyId) {
        this.key = new HmacKey(sharedSecret.getBytes());
        this.keyId = keyId;
    }

    @Override
    public String signMessage(String message) {
        int seq = signSeq;
        signSeq++;

        JwtClaims claims = new JwtClaims();
        claims.setClaim("message", message);
        claims.setClaim("messageSeq", seq);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        jws.setHeader("kid", keyId);
        jws.setKey(key);
        jws.setDoKeyValidation(false);

        try {
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new IllegalArgumentException("SharedSecretMessageSigner could not sign message " + message);
        }
    }

    @Override
    public String verifyMessage(String token) {
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setVerificationKey(key)
                .setRelaxVerificationKeyValidation()
                .setJwsAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT, AlgorithmIdentifiers.HMAC_SHA256))
                .build();

        try {
            JwtClaims claims = jwtConsumer.processToClaims(token);
            return claims.getClaimValueAsString("message");
        } catch (InvalidJwtException e) {
            throw new SignalingError(SignalingError.VERIFICATION_ERROR, "Cannot verify the JWT token: " + e.getMessage());
        }
    }
}
