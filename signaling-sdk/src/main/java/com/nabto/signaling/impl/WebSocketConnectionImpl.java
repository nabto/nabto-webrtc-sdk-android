package com.nabto.signaling.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.moshi.Moshi;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketConnectionImpl extends WebSocketListener implements WebSocketConnection {
    private final Logger logger = Logger.getLogger("WebSocketConnection");
    private final Moshi moshi = new Moshi.Builder().build();

    static class Message {
        public final String type = "MESSAGE";
        public String connectionId = "";
        public String message = "";
        public Boolean authorized = null;

        public Message(String connectionId, String message, Boolean authorized) {
            this.connectionId = connectionId;
            this.message = message;
            this.authorized = authorized;
        }

        @SuppressWarnings("unused")
        public Message() { }
    }

    static class MessagePeerConnected {
        public final String type = "PEER_CONNECTED";
        public String connectionId = "";

        public MessagePeerConnected(String connectionId) {
            this.connectionId = connectionId;
        }

        @SuppressWarnings("unused")
        public MessagePeerConnected() { }
    }

    static class MessagePeerOffline {
        public final String type = "PEER_OFFLINE";
        public String connectionId = "";

        public MessagePeerOffline(String connectionId) {
            this.connectionId = connectionId;
        }

        @SuppressWarnings("unused")
        public MessagePeerOffline() { }
    }

    static class MessageError {
        public final String type = "ERROR";
        public String connectionId = "";
        public String errorCode = "";

        public MessageError(String connectionId, String errorCode) {
            this.connectionId = connectionId;
            this.errorCode = errorCode;
        }

        @SuppressWarnings("unused")
        public MessageError() { }
    }

    static class MessagePing {
        final String type = "PING";
    }

    static class MessagePong {
        final String type = "PONG";
    }

    private int pongCounter = 0;
    private boolean isConnected = false;
    private final WebSocket ws;
    private final String name;
    private final WebSocketConnection.Observer observer;

    public WebSocketConnectionImpl(String name, String signalingUrl, WebSocketConnection.Observer observer) {
        this.name = name;
        this.observer = observer;
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url(signalingUrl)
                .build();

        this.ws = client.newWebSocket(request, this);
        client.dispatcher().executorService().shutdown();
    }

    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
        isConnected = true;
        observer.onOpen();
    }

    @Override
    public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        observer.onCloseOrError("closed");
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
        observer.onCloseOrError("error");
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        logger.info(name + " received websocket message\n" + text);
        // @TODO
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
        logger.info(name + " received a byte message of length " + bytes.size());
        // ignored
    }

    @Override
    public void sendMessage(String connectionId, String message) {
        Message msg = new Message(connectionId, message, null);
        var adapter = moshi.adapter(Message.class);
        this.send(adapter.toJson(msg));
    }

    @Override
    public void sendError(String connectionId, String errorCode) {
        MessageError err = new MessageError(connectionId, errorCode);
        var adapter = moshi.adapter(MessageError.class);
        this.send(adapter.toJson(err));
    }

    private void sendPing() {
        MessagePing msg = new MessagePing();
        var adapter = moshi.adapter(MessagePing.class);
        this.send(adapter.toJson(msg));
    }

    private void sendPong() {
        MessagePong msg = new MessagePong();
        var adapter = moshi.adapter(MessagePong.class);
        this.send(adapter.toJson(msg));
    }

    @Override
    public void checkAlive(int timeout) {
        var lastPongCounter = pongCounter;
        sendPing();
        new android.os.Handler().postDelayed(() -> {
            if (lastPongCounter == pongCounter) {
                observer.onCloseOrError("ping timeout");
            }
        }, timeout);
    }

    private void send(String msg) {
        if (!isConnected) {
            logger.info(name + " attempted to send a message before being connected. The message will be discarded.");
            return;
        }

        logger.info(name + " sending websocket message " + msg);
        this.ws.send(msg);
    }
}
