package com.nabto.webrtc.impl;

public enum RoutingMessageType {
    MESSAGE("MESSAGE"),
    ERROR("ERROR"),
    PEER_CONNECTED("PEER_CONNECTED"),
    PEER_OFFLINE("PEER_OFFLINE"),
    PING("PING"),
    PONG("PONG");

    private final String t;

    RoutingMessageType(final String t) {
        this.t = t;
    }

    public String text() {
        return t;
    }
}
