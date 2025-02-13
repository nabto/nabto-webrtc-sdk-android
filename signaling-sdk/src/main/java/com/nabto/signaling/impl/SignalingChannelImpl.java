package com.nabto.signaling.impl;

import com.nabto.signaling.SignalingChannel;
import com.nabto.signaling.SignalingChannelState;
import com.nabto.signaling.SignalingClient;
import com.nabto.signaling.SignalingError;

public class SignalingChannelImpl implements SignalingChannel {
    private SignalingChannelState channelState = SignalingChannelState.OFFLINE;
    private Reliability reliabilityLayer;
    private SignalingClientImpl signalingClient;
    private String channelId;

    public SignalingChannelImpl(SignalingClientImpl signalingClient, String channelId) {
        this.signalingClient = signalingClient;
        this.channelId = channelId;

        reliabilityLayer = new Reliability((msg) -> signalingClient.sendRoutingMessage(this.channelId, msg.toJsonString()));
    }

    @Override
    public void close() throws Exception {}

    @Override
    public SignalingChannelState getChannelState() {
        return channelState;
    }

    @Override
    public void sendMessage(String msg) {
        reliabilityLayer.sendReliableMessage(msg);
    }

    @Override
    public void sendError(String errorCode, String errorMessage) {

    }

    public void setChannelState(SignalingChannelState channelState) {
        this.channelState = channelState;
    }

    public void handleRoutingMessage(String message) {
        throw new UnsupportedOperationException();
    }

    public void handlePeerConnected() {
        throw new UnsupportedOperationException();
    }

    public void handlePeerOffline() {
        throw new UnsupportedOperationException();
    }

    public void handleError(SignalingError err) {
        throw new UnsupportedOperationException();
    }

    public void handleWebSocketConnect(boolean wasReconnected) {
        throw new UnsupportedOperationException();
    }
}
