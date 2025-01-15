package com.nabto.signaling.impl;

import com.nabto.signaling.SignalingChannel;
import com.nabto.signaling.SignalingChannelState;
import com.nabto.signaling.SignalingClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class SignalingClientImpl implements SignalingClient, WebSocketConnection.Observer {
    private final Logger logger = Logger.getLogger("SignalingClient");
    private final String endpointUrl;
    private final String productId;
    private final String deviceId;
    private final Backend backend;
    private WebSocketConnectionImpl ws = null;
    private final SignalingChannelImpl signalingChannel = new SignalingChannelImpl();
    private ConnectionState connectionState = ConnectionState.NOT_CONNECTED;
    private String connectionId = null;
    private String reconnectToken = null;
    private int reconnectCounter = 0;
    private int openedWebSockets = 0;

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
        if (connectionState != ConnectionState.NOT_CONNECTED) {
            future.completeExceptionally(new IllegalStateException("SignalingClientImpl.connect can only be called once!"));
            return future;
        }

        this.connectionState = ConnectionState.CONNECTING;
        backend.doClientConnect(accessToken).whenComplete((res, ex) -> {
            if (ex == null) {
                this.connectionId = res.connectionId;
                this.reconnectToken = res.reconnectToken;
                if (res.deviceOnline) {
                    signalingChannel.setChannelState(SignalingChannelState.ONLINE);
                }

                openWebsocketConnection(res.signalingUrl);
            } else {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    private void openWebsocketConnection(String signalingUrl) {
        ws = new WebSocketConnectionImpl("client", signalingUrl, this);
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public void onMessage(String connectionId, String message, boolean authorized) {
        signalingChannel.handleUnreliableMessage(message);
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
        signalingChannel.handleError(errorCode);
    }

    @Override
    public void onCloseOrError(String reason) {
        if (ws != null) {
            ws.close();
        }

        ws = null;
        connectionState = ConnectionState.WAIT_RETRY;

        long reconnectWait = 60000;
        if (reconnectCounter < 6) {
            reconnectWait = 1000 * (long)Math.pow(2, reconnectCounter);
        }
        reconnectCounter++;

        new android.os.Handler().postDelayed(this::reconnect, reconnectWait);
    }

    @Override
    public void onOpen() {
        signalingChannel.handlePeerConnected();
        reconnectCounter = 0;
        openedWebSockets++;
        if (openedWebSockets > 1) {
            signalingChannel.handleWebSocketReconnect();
        }
        connectionState = ConnectionState.CONNECTED;
    }

    private void reconnect() {
        throw new UnsupportedOperationException();
    }
}
