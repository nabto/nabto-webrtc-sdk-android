package com.nabto.signaling.impl;

import com.nabto.signaling.SignalingChannel;
import com.nabto.signaling.SignalingChannelState;
import com.nabto.signaling.SignalingClient;

public class SignalingChannelImpl implements SignalingChannel {
    private SignalingChannelState channelState = SignalingChannelState.OFFLINE;
    private SignalingClient signalingClient;

    public SignalingChannelImpl(SignalingClient signalingClient) {
        this.signalingClient = signalingClient;
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
