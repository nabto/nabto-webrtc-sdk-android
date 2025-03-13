package com.nabto.signaling;

public class SignalingMessage {
    // @TODO: proper implementation
    public String type;

    public SignalingMessage(String type) {
        this.type = type;
    }

    public String toJson() {
        return "{\"type\": \"" + type + "\"}";
    }
}
