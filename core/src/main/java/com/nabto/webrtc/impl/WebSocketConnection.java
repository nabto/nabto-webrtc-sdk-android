package com.nabto.webrtc.impl;

import org.json.JSONObject;

public interface WebSocketConnection {
    interface Observer {
        /**
         * Invoked when a new message arrives from the other peer.
         * @param connectionId The connection
         * @param message The message that was sent
         * @param authorized A boolean that is true if the peer is centrally authorized.
         */
        void onMessage(String connectionId, String message, boolean authorized);

        /**
         * Invoked when it has been detected that the remote peer has connected/reconnected.
         * @param connectionId The connection
         */
        void onPeerConnected(String connectionId);

        /**
         * Invoked when it has been detected that the remote peer has gone offline.
         * @param connectionId The connection
         */
        void onPeerOffline(String connectionId);

        /**
         * Called if the remote peer has sent an error over the connection.
         * @param connectionId The connection
         * @param errorCode The error code that was sent
         */
        void onConnectionError(String connectionId, String errorCode);

        /**
         * Called when the websocket connection has been closed or an error occurred.
         * @param reason A string describing why or how the websocket closed.
         */
        void onCloseOrError(String reason);

        /**
         * Called when the websocket has been opened.
         */
        void onOpen();

        /**
         * Called when the internal websocket implementation has failed.
         * @param t a Throwable describing why the websocket failed.
         */
        void onFailure(Throwable t);
    }

    void connect(String endpoint, Observer observer);

    /**
     * Send a message to the remote peer.
     * @param connectionId The connection
     * @param message The message to send
     */
    void sendMessage(String connectionId, JSONObject message);

    /**
     * Send an error code to the remote peer.
     * @param connectionId The connection
     * @param errorCode The error code to send over
     */
    void sendError(String connectionId, String errorCode);

    /**
     * Check if the websocket is still alive by sending an application layer ping.
     * This is used if it is detected that the WebRTC Connection detects a
     * connection problem and we do not know if the problem is with this peer or
     * the other peer.
     * @param timeout Timeout in milliseconds
     */
    void checkAlive(int timeout);
}
