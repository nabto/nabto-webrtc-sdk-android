package com.nabto.webrtc.impl;

import com.nabto.webrtc.SignalingChannelState;
import com.nabto.webrtc.SignalingClient;
import com.nabto.webrtc.SignalingConnectionState;
import com.nabto.webrtc.SignalingError;

import org.json.JSONException;
import org.json.JSONObject;

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
    private final String accessToken;
    private final boolean requireOnline;
    private final Backend backend;

    private final List<JSONObject> receivedMessages = new ArrayList<>();
    private SignalingChannelState channelState = SignalingChannelState.NEW;
    private Reliability reliabilityLayer;

    private boolean handlingReceivedMessages = false;

    private SignalingConnectionState connectionState = SignalingConnectionState.NEW;
    private String connectionId = null;
    private String signalingUrl = "";
    private boolean isReconnecting = false;
    private int reconnectCounter = 0;
    private int openedWebSockets = 0;
    private List<SignalingClient.Observer> observers = new ArrayList<>();

    private final WebSocketConnectionImpl webSocket = new WebSocketConnectionImpl();

    public SignalingClientImpl(String endpointUrl, String productId, String deviceId, boolean requireOnline, String accessToken) {
        if (productId.isEmpty() || deviceId.isEmpty()) {
            throw new IllegalArgumentException("SignalingClient was created with an empty product id or device id.");
        }

        if (endpointUrl.isEmpty()) {
            endpointUrl = "https://" + productId + ".webrtc.nabto.net";
        }

        this.endpointUrl = endpointUrl;
        this.productId = productId;
        this.deviceId = deviceId;
        this.requireOnline = requireOnline;
        this.accessToken = accessToken;

        reliabilityLayer = new Reliability((msg) -> this.sendRoutingMessage(msg.toJson()));
        backend = new Backend(endpointUrl, productId, deviceId);
    }

    @Override
    public void start() {
        this.connect();
    }

    private CompletableFuture<Void> connect() {
        var future = new CompletableFuture<Void>();
        if (connectionState != SignalingConnectionState.NEW) {
            future.completeExceptionally(new IllegalStateException("SignalingClient.connect has already been called previously!"));
            return future;
        }

        setConnectionState(SignalingConnectionState.CONNECTING);
        backend.doClientConnect(accessToken).whenComplete((res, ex) -> {
            if (ex == null) {
                this.connectionId = res.channelId;

                if (res.deviceOnline) {
                    setChannelState(SignalingChannelState.CONNECTED);
                } else {
                    setChannelState(SignalingChannelState.DISCONNECTED);
                    if (this.requireOnline) {
                        future.completeExceptionally(new RuntimeException("The requested device is not online."));
                        return;
                    }
                }

                this.signalingUrl = res.signalingUrl;
                openWebsocketConnection(res.signalingUrl);
                future.complete(null);
            } else {
                setConnectionState(SignalingConnectionState.FAILED);
                this.handleError(ex);
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
    public SignalingChannelState getChannelState() {
        return channelState;
    }

    @Override
    public void sendMessage(JSONObject msg) {
        reliabilityLayer.sendReliableMessage(msg);
    }

    @Override
    public void sendError(String errorCode, String errorMessage) {
        sendError(connectionId, errorCode, errorMessage);
    }

    @Override
    public void checkAlive() {
        webSocket.checkAlive(1000);
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
        webSocket.close();
        setConnectionState(SignalingConnectionState.CLOSED);
        setChannelState(SignalingChannelState.DISCONNECTED);
    }

    private void setConnectionState(SignalingConnectionState state) {
        this.connectionState = state;
        observers.forEach(obs -> obs.onConnectionStateChange(this.connectionState));
    }

    public void setChannelState(SignalingChannelState channelState) {
        this.channelState = channelState;
        observers.forEach((obs) -> obs.onChannelStateChange(channelState));
    }

    private void reconnect() {
        if (connectionState == SignalingConnectionState.FAILED || connectionState == SignalingConnectionState.CLOSED) {
            return;
        }

        isReconnecting = true;
        setConnectionState(SignalingConnectionState.CONNECTING);
        openWebsocketConnection(signalingUrl);
    }

    private void waitReconnect() {
        if (connectionState == SignalingConnectionState.FAILED || connectionState == SignalingConnectionState.CLOSED) {
            return;
        }

        if (connectionState == SignalingConnectionState.WAIT_RETRY) {
            return;
        }

        if (reconnectCounter > 7) {
            isReconnecting = false;
            setConnectionState(SignalingConnectionState.FAILED);
            return;
        }

        setConnectionState(SignalingConnectionState.WAIT_RETRY);
        var reconnectWait = 1000 * (1 << reconnectCounter);
        reconnectCounter++;
        new android.os.Handler().postDelayed(this::reconnect, reconnectWait);
    }

    private void openWebsocketConnection(String signalingUrl) {
        webSocket.connect(signalingUrl, new WebSocketConnection.Observer() {
            @Override
            public void onMessage(String connectionId, String message, boolean authorized) {
                handleRoutingMessage(message);
            }

            @Override
            public void onPeerConnected(String connectionId) {
                handlePeerConnected();
            }

            @Override
            public void onPeerOffline(String connectionId) {
                handlePeerOffline();
            }

            @Override
            public void onConnectionError(String connectionId, RoutingMessageError error) {
                var err = new SignalingError(error.errorCode, error.errorMessage, true);
                handleError(err);
            }

            @Override
            public void onCloseOrError(String errorCode) {
                if (connectionState == SignalingConnectionState.FAILED || connectionState == SignalingConnectionState.CLOSED) {
                    return;
                }

                if (openedWebSockets == 0) {
                    handleError(new SignalingError("WEBSOCKET_ERROR", errorCode));
                } else {
                    waitReconnect();
                }
            }

            @Override
            public void onOpen() {
                reconnectCounter = 0;
                openedWebSockets++;
                handleWebSocketConnect(openedWebSockets > 1);
                setConnectionState(SignalingConnectionState.CONNECTED);
            }

            @Override
            public void onFailure(java.lang.Throwable t) {
                if (isReconnecting) {
                    waitReconnect();
                }
            }
        });
    }

    // -------------------------------------------------------------
    // Websocket handler functions
    // -------------------------------------------------------------

    public void handleRoutingMessage(String message) {
        try {
            var parsed = ReliabilityData.fromJson(message);
            var reliableMessage = reliabilityLayer.handleRoutingMessage(parsed);
            receivedMessages.add(reliableMessage);
            handleReceivedMessages();
        } catch (JSONException e) {
            // @TODO: Logging
        }
    }

    public void handlePeerConnected() {
        setChannelState(SignalingChannelState.CONNECTED);
        reliabilityLayer.handlePeerConnected();
    }

    public void handlePeerOffline() {
        setChannelState(SignalingChannelState.DISCONNECTED);
    }

    public void handleError(java.lang.Throwable error) {
        if (channelState == SignalingChannelState.CLOSED || channelState == SignalingChannelState.FAILED) {
            return;
        }
        observers.forEach((obs) -> obs.onError(error));
    }

    public void handleWebSocketConnect(boolean wasReconnected) {
        if (channelState == SignalingChannelState.CLOSED || channelState == SignalingChannelState.FAILED) {
            return;
        }
        reliabilityLayer.handleConnect();
        if (wasReconnected) {
            observers.forEach(SignalingClient.Observer::onConnectionReconnect);
        }
    }

    private void handleReceivedMessages() {
        if (!handlingReceivedMessages)
        {
            if (!receivedMessages.isEmpty()) {
                handlingReceivedMessages = true;
                var msg = receivedMessages.remove(0);
                if (msg != null) {
                    for (var obs: observers) {
                        obs.onMessage(msg);
                    }
                }
                handlingReceivedMessages = false;
                handleReceivedMessages();
            }
        }
    }

    public void sendRoutingMessage(JSONObject message) {
        webSocket.sendMessage(this.connectionId, message);
    }

    public void sendError(String channelId, String errorCode, String errorMessage) {
        webSocket.sendError(channelId, errorCode, errorMessage);
    }
}
