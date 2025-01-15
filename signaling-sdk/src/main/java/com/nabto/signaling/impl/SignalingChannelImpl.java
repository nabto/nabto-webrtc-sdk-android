package com.nabto.signaling.impl;

import com.nabto.signaling.SignalingChannel;
import com.nabto.signaling.SignalingChannelState;

public class SignalingChannelImpl implements SignalingChannel {
    private SignalingChannelState channelState = SignalingChannelState.OFFLINE;

    @Override
    public void close() throws Exception {}

    @Override
    public SignalingChannelState getChannelState() {
        return channelState;
    }

    public void setChannelState(SignalingChannelState channelState) {
        this.channelState = channelState;
    }

    public void handleUnreliableMessage(String message) {
        throw new UnsupportedOperationException();
    }

    public void handlePeerConnected() {
        throw new UnsupportedOperationException();
    }

    public void handlePeerOffline() {
        throw new UnsupportedOperationException();
    }

    public void handleError(String errorCode) {
        throw new UnsupportedOperationException();
    }

    public void handleWebSocketReconnect() {
        throw new UnsupportedOperationException();
    }
}
