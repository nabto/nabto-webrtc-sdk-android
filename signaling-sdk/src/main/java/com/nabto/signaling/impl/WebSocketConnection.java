package com.nabto.signaling.impl;

/**
 * @TODO: Documentation
 * @TODO: Should this extend AutoCloseable?
 */
public interface WebSocketConnection {
    /**
     * @TODO: Documentation
     */
    interface Observer {
        /**
         * Invoked when a new message arrives from the other peer.
         * @param connectionId @TODO
         * @param message @TODO
         * @param authorized @TODO
         */
        void onMessage(String connectionId, String message, boolean authorized);

        /**
         * Invoked when it has been detected that the remote peer has connected/reconnected.
         * @param connectionId @TODO
         */
        void onPeerConnected(String connectionId);

        /**
         * Invoked if it is detected that the remote peer is offline.
         * @param connectionId @TODO
         */
        void onPeerOffline(String connectionId);

        /**
         * Called if the remote peer has sent an error over the connection.
         * @param connectionId @TODO
         * @param errorCode @TODO
         */
        void onConnectionError(String connectionId, String errorCode);

        /**
         * Called when the websocket connection has been closed or an error occurred.
         * @param reason @TODO
         */
        void onCloseOrError(String reason);

        /**
         * Called when the websocket has been opened.
         */
        void onOpen();
    }

    /**
     * Send a message to the remote peer.
     * @param connectionId @TODO
     * @param message @TODO
     */
    void sendMessage(String connectionId, String message);

    /**
     * Send an error code to the remote peer.
     * @param connectionId @TODO
     * @param errorCode @TODO
     */
    void sendError(String connectionId, String errorCode);

    /**
     * Check if the websocket is still alive by sending an application layer ping.
     * This is used if it is detected that the WebRTC Connection detects a
     * connection problem and we do not know if the problem is with this peer or
     * the other peer.
     * @param timeout @TODO
     */
    void checkAlive(int timeout);
}
