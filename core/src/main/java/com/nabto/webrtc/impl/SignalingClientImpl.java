package com.nabto.webrtc.impl;

import com.nabto.webrtc.SignalingChannelState;
import com.nabto.webrtc.SignalingClient;
import com.nabto.webrtc.SignalingConnectionState;
import com.nabto.webrtc.SignalingError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SignalingClientImpl implements SignalingClient {
    private final String accessToken;
    private final boolean requireOnline;
    private final Backend backend;

    private SignalingChannelState channelState = SignalingChannelState.NEW;
    final private Reliability reliabilityLayer;

    private SignalingConnectionState connectionState = SignalingConnectionState.NEW;
    private String connectionId = null;
    private String websocketSignalingUrl = "";
    private boolean isReconnecting = false;
    private int reconnectCounter = 0;
    private int openedWebSockets = 0;
    final private Set<Observer> observers = ConcurrentHashMap.newKeySet();

    private final WebSocketConnectionImpl webSocket = new WebSocketConnectionImpl();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public SignalingClientImpl(String endpointUrl, String productId, String deviceId, boolean requireOnline, String accessToken) {
        if (productId.isEmpty() || deviceId.isEmpty()) {
            throw new IllegalArgumentException("SignalingClient was created with an empty product id or device id.");
        }

        if (endpointUrl.isEmpty()) {
            endpointUrl = "https://" + productId + ".webrtc.nabto.net";
        }

        this.requireOnline = requireOnline;
        this.accessToken = accessToken;

        reliabilityLayer = new Reliability((msg) -> this.sendRoutingMessage(msg.toJson()));
        backend = new Backend(endpointUrl, productId, deviceId);
    }

    @Override
    public synchronized void start() {
        if (connectionState != SignalingConnectionState.NEW) {
            throw new IllegalStateException("SignalingClient.connect has already been called previously!");
        }
        setConnectionState(SignalingConnectionState.CONNECTING);
        this.connect();
    }

    private CompletableFuture<Void> connect() {
        var future = new CompletableFuture<Void>();
        backend.doClientConnect(accessToken).whenComplete((res, ex) -> {
            if (ex == null) {
                this.connectionId = res.channelId;

                if (res.deviceOnline) {
                    setChannelState(SignalingChannelState.CONNECTED);
                } else {
                    setChannelState(SignalingChannelState.DISCONNECTED);
                    if (this.requireOnline) {
                        Throwable deviceOfflineException = new DeviceOfflineException();
                        this.handleError(deviceOfflineException);
                        future.completeExceptionally(deviceOfflineException);
                        return;
                    }
                }

                this.websocketSignalingUrl = res.signalingUrl;
                openWebsocketConnection(res.signalingUrl);
                future.complete(null);
            } else {
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
        if (connectionState != SignalingConnectionState.FAILED && connectionState != SignalingConnectionState.CLOSED) {
            reliabilityLayer.sendReliableMessage(msg);
        } else {
            throw new RuntimeException("trying to send a message in a closed or failed state.");
        }
    }

    @Override
    public void sendError(SignalingError signalingError) {
        if (connectionState != SignalingConnectionState.FAILED && connectionState != SignalingConnectionState.CLOSED) {
            sendError(connectionId, signalingError);
        }
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
    public synchronized void close() {
        if (connectionState == SignalingConnectionState.CLOSED) {
            return;
        }
        if (connectionState != SignalingConnectionState.FAILED) {
            sendError(new SignalingError(SignalingError.CHANNEL_CLOSED, "The channel has been closed by the application."));
        }
        setConnectionState(SignalingConnectionState.CLOSED);
        webSocket.close();
        setChannelState(SignalingChannelState.DISCONNECTED);
        scheduledExecutorService.shutdown();
    }

    private void setConnectionState(SignalingConnectionState state) {
        this.connectionState = state;
        observers.forEach(obs -> obs.onConnectionStateChange(this.connectionState));
    }

    private void setChannelState(SignalingChannelState channelState) {
        this.channelState = channelState;
        observers.forEach((obs) -> obs.onChannelStateChange(channelState));
    }

    private void reconnect() {
        if (connectionState == SignalingConnectionState.FAILED || connectionState == SignalingConnectionState.CLOSED) {
            return;
        }

        isReconnecting = true;
        setConnectionState(SignalingConnectionState.CONNECTING);
        openWebsocketConnection(websocketSignalingUrl);
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
        // TODO use jitter
        var reconnectWait = 1000 * (1 << reconnectCounter);
        reconnectCounter++;
        scheduledExecutorService.schedule(this::reconnect, reconnectWait, TimeUnit.MILLISECONDS);
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
            public void onConnectionError(String connectionId, SignalingError error) {
                handleError(error);
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
                if (openedWebSockets == 0) {
                    handleError(t);
                } else {
                    if (isReconnecting) {
                        waitReconnect();
                    }
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
            if (reliableMessage != null) {
                for (var obs: observers) {
                    obs.onMessage(reliableMessage);
                }
            }
        } catch (JSONException e) {
            this.handleError(e);
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
        setConnectionState(SignalingConnectionState.FAILED);
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

    public void sendRoutingMessage(JSONObject message) {
        webSocket.sendMessage(this.connectionId, message);
    }

    public void sendError(String channelId, SignalingError signalingError) {
        webSocket.sendError(channelId, signalingError);
    }
}
