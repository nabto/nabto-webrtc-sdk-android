package com.nabto.signaling.impl;

public class ReliabilityMessage {
    public enum MessageType {
        ACK,
        MESSAGE
    }

    public MessageType type;
    public int seq;
    public String message;

    public ReliabilityMessage(MessageType type, int seq, String message) {
        this.type = type;
        this.seq = seq;
        this.message = message;
    }
}
