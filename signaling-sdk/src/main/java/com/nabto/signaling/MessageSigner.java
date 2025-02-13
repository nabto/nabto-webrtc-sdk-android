package com.nabto.signaling;

public interface MessageSigner {
    String signMessage(String message);
    String verifyMessage(String token);
}
