package com.nabto.signaling.impl;

import com.nabto.signaling.SignalingChannel;
import com.nabto.signaling.SignalingChannelState;
import com.nabto.signaling.SignalingError;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class SignalingChannelImpl implements SignalingChannel, AutoCloseable {
    private final List<String> receivedMessages = new ArrayList<>();
    private SignalingChannelState channelState = SignalingChannelState.OFFLINE;
    private final Reliability reliabilityLayer;
    private final SignalingClientImpl signalingClient;
    private final String channelId;
    private boolean closed = false;

    public SignalingChannelImpl(SignalingClientImpl signalingClient, String channelId) {
        this.signalingClient = signalingClient;
        this.channelId = channelId;

        reliabilityLayer = new Reliability((msg) -> signalingClient.sendRoutingMessage(this.channelId, msg.toJsonString()));
    }

    @Override
    public void close() throws Exception {
        if (closed) return;
        closed = true;
        signalingClient.sendError(this.channelId, "CHANNEL_CLOSED", ""); // @TODO
        signalingClient.close();
    }

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
        signalingClient.sendError(channelId, errorCode, errorMessage);
    }

    public void setChannelState(SignalingChannelState channelState) {
        this.channelState = channelState;
    }

    public void handleRoutingMessage(String message) {
        try {
            var parsed = ReliabilityMessage.fromJson(message);
            var reliableMessage = reliabilityLayer.handleRoutingMessage(parsed);
            receivedMessages.add(reliableMessage);
            handleReceivedMessages();
        } catch (JSONException e) {
            // @TODO: Logging
        }
    }

    public void handlePeerConnected() {
        channelState = SignalingChannelState.ONLINE;
        reliabilityLayer.handlePeerConnected();
    }

    public void handlePeerOffline() {
        channelState = SignalingChannelState.OFFLINE;
    }

    public void handleError(SignalingError err) {
        // @TODO: Emit to a callback?
    }

    public void handleWebSocketConnect(boolean wasReconnected) {
        reliabilityLayer.handleConnect();
        if (wasReconnected) {
            // @TODO: Emit to a reconnect callback?
        }
    }

    private void handleReceivedMessages() {
        // @TODO
    }
}
