package com.nabto.signaling.impl;

import android.util.Log;

import com.nabto.signaling.SignalingChannel;
import com.nabto.signaling.SignalingChannelState;
import com.nabto.signaling.SignalingClient;
import com.nabto.signaling.SignalingConnectionState;
import com.nabto.signaling.SignalingError;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class SignalingClientImpl implements SignalingClient {
    private final Logger logger = Logger.getLogger("SignalingClient");
    private boolean closed = false;

    private final String endpointUrl;
    private final String productId;
    private final String deviceId;
    private final Backend backend;

    private SignalingConnectionState connectionState = SignalingConnectionState.NEW;
    private String connectionId = null;
    private String reconnectToken = null;
    private int reconnectCounter = 0;
    private int openedWebSockets = 0;
    private List<SignalingClient.Observer> observers = new ArrayList<>();

    private final WebSocketConnectionImpl webSocket = new WebSocketConnectionImpl();
    private final SignalingChannelImpl signalingChannel = new SignalingChannelImpl(this, "not_connected");

    public SignalingClientImpl(String endpointUrl, String productId, String deviceId) {
        this.endpointUrl = endpointUrl;
        this.productId = productId;
        this.deviceId = deviceId;

        backend = new Backend(endpointUrl, productId, deviceId);
    }

    @Override
    public SignalingChannel getSignalingChannel() {
        return signalingChannel;
    }

    @Override
    public CompletableFuture<Void> connect() {
        return connect(null);
    }

    @Override
    public CompletableFuture<Void> connect(String accessToken) {
        var future = new CompletableFuture<Void>();
        if (connectionState != SignalingConnectionState.NEW) {
            future.completeExceptionally(new IllegalStateException("SignalingClientImpl.connect can only be called once!"));
            return future;
        }

        setConnectionState(SignalingConnectionState.CONNECTING);
        backend.doClientConnect(accessToken).whenComplete((res, ex) -> {
            if (ex == null) {
                this.connectionId = res.channelId;
                this.reconnectToken = res.reconnectToken;

                signalingChannel.setChannelId(this.connectionId);
                if (res.deviceOnline) {
                    signalingChannel.setChannelState(SignalingChannelState.ONLINE);
                }

                openWebsocketConnection(res.signalingUrl);
                future.complete(null);
            } else {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    @Override
    public SignalingConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    public void addObserver(Observer obs) {
        observers.add(obs);
    }

    @Override
    public void removeObserver(Observer obs) {
        observers.remove(obs);
    }

    @Override
    public void close() throws Exception {
        if (closed) return;
        closed = true;
        signalingChannel.close();
        webSocket.close();
        setConnectionState(SignalingConnectionState.CLOSED);
    }

    private void setConnectionState(SignalingConnectionState state) {
        this.connectionState = state;
        observers.forEach(obs -> obs.onConnectionStateChange(this.connectionState));
    }

    private void waitReconnect() {
        // @TODO: implementation
    }

    private void openWebsocketConnection(String signalingUrl) {
        webSocket.connect(signalingUrl, new WebSocketConnection.Observer() {
            @Override
            public void onMessage(String connectionId, String message, boolean authorized) {
                signalingChannel.handleRoutingMessage(message);
            }

            @Override
            public void onPeerConnected(String connectionId) {
                signalingChannel.handlePeerConnected();
            }

            @Override
            public void onPeerOffline(String connectionId) {
                signalingChannel.handlePeerOffline();
            }

            @Override
            public void onConnectionError(String connectionId, String errorCode) {
                var err = new SignalingError(errorCode, "", true); // @TODO: error message
                signalingChannel.handleError(err);
            }

            @Override
            public void onCloseOrError(String errorCode) {
                waitReconnect();
            }

            @Override
            public void onOpen() {
                reconnectCounter = 0;
                openedWebSockets++;
                signalingChannel.handleWebSocketConnect(openedWebSockets > 1);
                setConnectionState(SignalingConnectionState.CONNECTED);
            }
        });
    }

    public void sendRoutingMessage(String channelId, String message) {
        webSocket.sendMessage(channelId, message);
    }

    public void sendError(String channelId, String errorCode, String errorMessage) {
        webSocket.sendError(channelId, errorCode); // @TODO: send errorMessage
    }
}
