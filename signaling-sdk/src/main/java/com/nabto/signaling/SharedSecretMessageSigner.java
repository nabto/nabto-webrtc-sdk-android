package com.nabto.signaling.impl;

import com.nabto.signaling.MessageSigner;

public class SharedSecretMessageSigner implements MessageSigner {
    private String sharedSecret;
    private String keyId;
    private int signSeq = 0;
    private int verifySeq = 0;

    public SharedSecretMessageSigner(String sharedSecret, String keyId) {
        this.sharedSecret = sharedSecret;
        this.keyId = keyId;
    }

    @Override
    public String signMessage(String message) {
        return "";
    }

    @Override
    public String verifyMessage(String token) {
        return "";
    }
}
