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
}
