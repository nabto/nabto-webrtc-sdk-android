package com.nabto.webrtc.util;

public interface MessageSigner {
    String signMessage(String message);
    String verifyMessage(String token);
}
