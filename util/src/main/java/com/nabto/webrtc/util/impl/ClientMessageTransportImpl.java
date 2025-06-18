package com.nabto.webrtc.util.impl;

import com.nabto.webrtc.SignalingChannelState;
import com.nabto.webrtc.SignalingClient;
import com.nabto.webrtc.SignalingConnectionState;
import com.nabto.webrtc.SignalingError;
import com.nabto.webrtc.util.JWTMessageSigner;
import com.nabto.webrtc.util.MessageSigner;
import com.nabto.webrtc.util.MessageTransport;
import com.nabto.webrtc.util.NoneMessageSigner;
import com.nabto.webrtc.util.SignalingIceServer;
import com.nabto.webrtc.util.SignalingMessage;
import com.nabto.webrtc.util.SignalingMessageUnion;
import com.nabto.webrtc.util.SignalingSetupRequest;

import org.json.JSONObject;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;


public class ClientMessageTransportImpl implements MessageTransport {
    private enum State {
        SETUP,
        SIGNALING
    }
    private SignalingClient client;
    private MessageSigner messageSigner;
    private Set<MessageTransport.Observer> observers = ConcurrentHashMap.newKeySet();
    private State state = State.SETUP;
    public static ClientMessageTransportImpl createSharedSecretMessageTransport(SignalingClient client, String sharedSecret, Optional<String> keyId) {
        var messageSigner = new JWTMessageSigner(sharedSecret, keyId);
        var transport = new ClientMessageTransportImpl(client, messageSigner);
        transport.start();
        return transport;
    }
    public static ClientMessageTransportImpl createNoneMessageTransport(SignalingClient client) {
        var messageSigner = new NoneMessageSigner();
        var transport = new ClientMessageTransportImpl(client, messageSigner);
        transport.start();
        return transport;
    }

    private ClientMessageTransportImpl(SignalingClient client, MessageSigner messageSigner) {
        this.client = client;
        this.messageSigner = messageSigner;
    }

    public void addObserver(MessageTransport.Observer observer) {
        this.observers.add(observer);
    }

    public void removeObserver(MessageTransport.Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public Mode getMode() {
        return Mode.CLIENT;
    }

    @Override
    public void sendWebRTCSignalingMessage(SignalingMessage message) {
        sendSignalingMessage(message);
    }

    private void sendSignalingMessage(SignalingMessage message) {
        var encoded = message.toJson();
        var signed = messageSigner.signMessage(encoded);
        this.client.sendMessage(signed);
    }

    private void handleMessage(JSONObject message) {
        try {
            var verified = this.messageSigner.verifyMessage(message);
            var decoded = SignalingMessageUnion.fromJson(verified);
            if (state == State.SETUP) {
                if (decoded.isSetupResponse()) {
                    var setupResponse = decoded.getSetupResponse();
                    this.state = State.SIGNALING;
                    emitSetupDone(setupResponse.iceServers);
                    return;
                }
            } else if (state == State.SIGNALING) {
                if (decoded.isCandidate() || decoded.isDescription()) {
                    this.emitWebRTCSignalingMessage(decoded);
                    return;
                }
            }
            throw new RuntimeException(String.format("Unhandled message %s", message.toString()));
        } catch (Exception e) {
            emitError(e);
        }
    }

    private void emitError(Exception e) {
        this.observers.forEach( observer -> {
            observer.onError(e);
        });
    }

    private void emitSetupDone(List<SignalingIceServer> iceServers) {
        this.observers.forEach( observer -> {
            observer.onSetupDone(iceServers);
        });
    }

    private void emitWebRTCSignalingMessage(SignalingMessageUnion message) {
        this.observers.forEach( observer -> {
            observer.onWebRTCSignalingMessage(message);
        });
    }

    private void start() {
        this.client.addObserver(new SignalingClient.Observer() {
            @Override
            public void onConnectionStateChange(SignalingConnectionState newState) {

            }

            @Override
            public void onMessage(JSONObject message) {
                handleMessage(message);
            }

            @Override
            public void onChannelStateChange(SignalingChannelState newState) {

            }

            @Override
            public void onConnectionReconnect() {

            }

            @Override
            public void onError(SignalingError error) {

            }
        });
        this.sendSignalingMessage(new SignalingSetupRequest());
    }
}
