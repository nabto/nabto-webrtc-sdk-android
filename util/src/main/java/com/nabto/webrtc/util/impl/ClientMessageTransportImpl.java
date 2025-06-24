package com.nabto.webrtc.util.impl;

import com.nabto.webrtc.SignalingClient;
import com.nabto.webrtc.util.MessageTransport;
import com.nabto.webrtc.util.SignalingIceServer;
import com.nabto.webrtc.util.WebRTCSignalingMessage;
import com.nabto.webrtc.util.WebRTCSignalingMessageUnion;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;


public class ClientMessageTransportImpl implements MessageTransport {
    private enum State {
        SETUP,
        SIGNALING
    }
    final private SignalingClient client;
    final private MessageSigner messageSigner;
    final private Set<MessageTransport.Observer> observers = ConcurrentHashMap.newKeySet();
    private State state = State.SETUP;
    public static ClientMessageTransportImpl createSharedSecretMessageTransport(SignalingClient client, String sharedSecret, String keyId) {
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
    public void sendWebRTCSignalingMessage(WebRTCSignalingMessage message) {
        sendSignalingMessage(message);
    }

    private void sendSignalingMessage(SignalingMessage message) {
        var encoded = message.toJson();
        var signed = messageSigner.signMessage(encoded);
        this.client.sendMessage(signed);
    }

    /**
     * We will only receive one message at a time from the core signaling client.
     * This means we do not have to handle a case with concurrent messages and potentially
     * swapped orders of the messages.
     *
     * @param message the signaling message to handle
     */
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
                if (decoded.isCandidate()) {
                    this.emitWebRTCSignalingMessage(new WebRTCSignalingMessageUnion(decoded.getCandidate()));
                    return;
                }
                if (decoded.isDescription()) {
                    this.emitWebRTCSignalingMessage(new WebRTCSignalingMessageUnion(decoded.getDescription()));
                    return;
                }
            }
            throw new RuntimeException(String.format("Unhandled message %s", message.toString()));
        } catch (Exception e) {
            emitError(e);
        }
    }

    private void emitError(Exception e) {
        this.observers.forEach( observer -> observer.onError(e));
    }

    private void emitSetupDone(List<SignalingIceServer> iceServers) {
        this.observers.forEach( observer -> observer.onSetupDone(iceServers));
    }

    private void emitWebRTCSignalingMessage(WebRTCSignalingMessageUnion message) {
        this.observers.forEach( observer -> observer.onWebRTCSignalingMessage(message));
    }

    private void start() {
        this.client.addObserver(new SignalingClient.AbstractObserver() {
            @Override
            public void onMessage(JSONObject message) {
                handleMessage(message);
            }
        });
        this.sendSignalingMessage(new SignalingSetupRequest());
    }
}
