package com.nabto.webrtc.impl;

import com.nabto.webrtc.SignalingChannel;
import com.nabto.webrtc.SignalingChannelState;
import com.nabto.webrtc.SignalingError;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SignalingChannelImpl implements SignalingChannel, AutoCloseable {
    private final Logger logger = Logger.getLogger("SignalingChannel");
    private final List<String> receivedMessages = new ArrayList<>();
    private SignalingChannelState channelState = SignalingChannelState.NEW;
    private final Reliability reliabilityLayer;
    private final SignalingClientImpl signalingClient;
    private String channelId;
    private boolean closed = false;

    private boolean handlingReceivedMessages = false;

    private final List<SignalingChannel.Observer> observers = new ArrayList<>();

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

    @Override
    public void checkAlive() {

    }

    @Override
    public void addObserver(Observer obs) {
        observers.add(obs);
    }

    @Override
    public void removeObserver(Observer obs) {
        observers.remove(obs);
    }

    public void setChannelState(SignalingChannelState channelState) {
        this.channelState = channelState;
        observers.forEach((obs) -> obs.onChannelStateChange(channelState));
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
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
        setChannelState(SignalingChannelState.ONLINE);
        reliabilityLayer.handlePeerConnected();
    }

    public void handlePeerOffline() {
        setChannelState(SignalingChannelState.OFFLINE);
    }

    public void handleError(SignalingError error) {
        if (channelState == SignalingChannelState.CLOSED || channelState == SignalingChannelState.FAILED) {
            return;
        }
        observers.forEach((obs) -> obs.onSignalingError(error));
    }

    public void handleWebSocketConnect(boolean wasReconnected) {
        if (channelState == SignalingChannelState.CLOSED || channelState == SignalingChannelState.FAILED) {
            return;
        }
        reliabilityLayer.handleConnect();
        if (wasReconnected) {
            observers.forEach(Observer::onSignalingReconnect);
        }
    }

    private void handleReceivedMessages() {
        // @TODO: validate this is correct
        if (!handlingReceivedMessages)
        {
            if (!receivedMessages.isEmpty()) {
                handlingReceivedMessages = true;
                var msg = receivedMessages.remove(0);
                if (msg != null) {
                    observers.forEach(obs -> obs.onMessage(msg));
                }
                handlingReceivedMessages = false;
                handleReceivedMessages();
            }
        }
    }
}
