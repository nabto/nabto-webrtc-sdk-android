package com.nabto.webrtc.impl;

public class RoutingMessage {
    public enum MessageType {
        MESSAGE,
        ERROR,
        PEER_CONNECTED,
        PEER_OFFLINE,
        PING,
        PONG
    }

    public MessageType type;
    public String channelId;
    public String message;
    public boolean authorized;
    public String errorCode;
    public String errorMessage;

    public RoutingMessage(MessageType type, String channelId, String message, boolean authorized, String errorCode, String errorMessage) {
        this.type = type;
        this.channelId = channelId;
        this.message = message;
        this.authorized = authorized;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
