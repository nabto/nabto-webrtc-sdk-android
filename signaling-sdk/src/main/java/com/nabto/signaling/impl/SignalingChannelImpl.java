package com.nabto.signaling.impl;

import com.nabto.signaling.SignalingChannel;
import com.nabto.signaling.SignalingChannelState;
import com.nabto.signaling.SignalingClient;

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

    }

    @Override
    public void sendError(String errorCode, String errorMessage) {

    }

    public void setChannelState(SignalingChannelState channelState) {
        this.channelState = channelState;
    }
}
