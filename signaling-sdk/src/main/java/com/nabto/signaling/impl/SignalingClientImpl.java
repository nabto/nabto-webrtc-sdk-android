package com.nabto.signaling.impl;

import com.nabto.signaling.SignalingChannel;
import com.nabto.signaling.SignalingChannelState;
import com.nabto.signaling.SignalingClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class SignalingClientImpl implements SignalingClient {
    private final Logger logger = Logger.getLogger("SignalingClient");

    private final String endpointUrl;
    private final String productId;
    private final String deviceId;
    private final Backend backend;

    private ConnectionState connectionState = ConnectionState.NOT_CONNECTED;
    private String connectionId = null;
    private String reconnectToken = null;

    private final SignalingChannelImpl signalingChannel = new SignalingChannelImpl();

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

    @Override
    public void close() throws Exception {

    }

    private void openWebsocketConnection(String signalingUrl) {
        // @TODO: Implementation
        throw new UnsupportedOperationException();
    }
}
