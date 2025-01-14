package com.nabto.signaling.impl;

public class WebSocketConnectionImpl implements WebSocketConnection {
    enum WebSocketMessageType {
        MESSAGE,
        ERROR,
        PEER_CONNECTED,
        PEER_OFFLINE,
        PING,
        PONG
    }

    @Override
    public void sendMessage(String connectionId, String message) {

    }

    @Override
    public void sendError(String connectionId, String errorCode) {

    }

    @Override
    public void checkAlive(int timeout) {

    }
}
