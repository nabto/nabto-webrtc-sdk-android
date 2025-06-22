package com.nabto.webrtc.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketConnectionImpl extends WebSocketListener implements WebSocketConnection {
    private final Logger logger = Logger.getLogger("SignalingClient");

    private boolean isConnected = false;
    private int pongCounter = 0;
    private WebSocket ws = null;
    Observer observer;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void connect(String endpoint, Observer observer) {
        this.observer = observer;
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build();
        Request request = new Request.Builder().url(endpoint).build();
        ws = client.newWebSocket(request, this);
        client.dispatcher().executorService().shutdown();
    }

    @Override
    public void sendMessage(String channelId, JSONObject message) {
        send(new RoutingMessage(
                RoutingMessageType.MESSAGE,
                channelId,
                message,
                false,
                null,
                null
        ));
    }

    @Override
    public void sendError(String channelId, String errorCode, String errorMessage) {
        send(new RoutingMessage(
                RoutingMessageType.ERROR,
                channelId,
                null,
                false,
                errorCode,
                errorMessage
        ));
    }

    @Override
    public void checkAlive(int timeout) {
        var currentPongCounter = pongCounter;
        sendPing();
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                if (currentPongCounter == pongCounter) {
                    observer.onCloseOrError("Ping timeout");
                }
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
        super.onOpen(webSocket, response);
        isConnected = true;
        observer.onOpen();
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        super.onMessage(webSocket, text);
        logger.info("Received message " + text);
        try {
            JSONObject json = new JSONObject(text);
            var type = json.getString("type");
            if (Objects.equals(type, RoutingMessageType.MESSAGE.text())) {
                var channelId = json.getString("channelId");
                var msg = json.getString("message");
                var authorized = json.optBoolean("authorized", false);
                observer.onMessage(channelId, msg, authorized);
            }

            if (Objects.equals(type, RoutingMessageType.ERROR.text())) {
                var channelId = json.getString("channelId");
                var errorJson = json.getJSONObject("error");
                var errorCode = errorJson.getString("code");
                var errorMessage = errorJson.optString("message");
                observer.onConnectionError(channelId, new RoutingMessageError(errorCode, errorMessage));
            }

            if (Objects.equals(type, RoutingMessageType.PEER_CONNECTED.text())) {
                var channelId = json.getString("channelId");
                observer.onPeerConnected(channelId);
            }

            if (Objects.equals(type, RoutingMessageType.PEER_OFFLINE.text())) {
                var channelId = json.getString("channelId");
                observer.onPeerOffline(channelId);
            }

            if (Objects.equals(type, RoutingMessageType.PING.text())) {
                sendPong();
            }

            if (Objects.equals(type, RoutingMessageType.PONG.text())) {
                pongCounter++;
            }
        } catch (JSONException e) {
            logger.warning("Cannot parse message as json.");
        }
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
        super.onMessage(webSocket, bytes);
        logger.warning("Received unexpected binary message.");
    }

    @Override
    public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosing(webSocket, code, reason);
        webSocket.close(1000, null);
        logger.info("Closing websocket...");
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosed(webSocket, code, reason);
        observer.onCloseOrError("closed");
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        observer.onFailure(t);
    }

    public void close() {
        if (ws != null) {
            ws.close(1000, null);
        }
        scheduledExecutorService.shutdown();
    }

    public void sendPing() {
        send(new RoutingMessage(
                RoutingMessageType.PING,
                null,
                null,
                false,
                null,
                null
        ));
    }

    private void sendPong() {
        send(new RoutingMessage(
                RoutingMessageType.PONG,
                null,
                null,
                false,
                null,
                null
        ));
    }

    private void send(RoutingMessage msg) {
        if (!isConnected) {
            return;
        }

        try {
            JSONObject json = new JSONObject();
            switch (msg.type) {
                case MESSAGE: {
                    json.put("type", RoutingMessageType.MESSAGE.text());
                    json.put("channelId", msg.channelId);
                    json.put("message", msg.message);
                    break;
                }

                case ERROR: {
                    json.put("type", RoutingMessageType.ERROR.text());
                    json.put("channelId", msg.channelId);
                    json.put("errorCode", msg.errorCode);
                    if (msg.errorMessage != null) {
                        json.put("errorMessage", msg.errorMessage);
                    }
                    break;
                }

                case PING: {
                    json.put("type", RoutingMessageType.PING.text());
                    break;
                }

                case PONG: {
                    json.put("type", RoutingMessageType.PONG.text());
                    break;
                }

                default: {
                    logger.warning("Tried to send unimplemented message in WebSocketConnectionImpl");
                    return;
                }
            }

            if (ws != null) {
                ws.send(json.toString());
            }
        } catch (JSONException e) {
            logger.warning("Failed to send message due to " + e.getMessage());
        }
    }
}
