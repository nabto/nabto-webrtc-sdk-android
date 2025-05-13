package com.nabto.webrtc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

/**
 * SignalingClient is responsible for creating a signaling connection through
 * the Nabto Signaling Service for WebRTC.
 */
public interface SignalingClient extends AutoCloseable {
    /**
     * Observer interface for callbacks
     */
    interface Observer {
        /**
         * Callback invoked when the connection state changes
         * @param newState The new connection state
         */
        void onConnectionStateChange(SignalingConnectionState newState);
    }

    /**
     * Get the associated {@link SignalingChannel} for this client.
     * @return The signaling channel.
     */
    SignalingChannel getSignalingChannel();

    /**
     * Asynchronously attempt to make an anonymous connection to the signaling service.
     * @return {@link CompletionStage} that will be completed when the connection is established or an error occurs.
     */
    CompletableFuture<Void> connect();

    /**
     * Asynchronously attempt to make an authorized connection to the signaling service.
     * @param accessToken Access token that will be used to establish an authorized connection.
     * @return {@link Future} that will be completed when the connection is established or an error occurs.
     */
    CompletableFuture<Void> connect(String accessToken);

    // @TODO: conncetion state change callbacks?

    /**
     * Get the current state of the Signaling Connection
     * @return The current state of the connection
     */
    SignalingConnectionState getConnectionState();

    /**
     * Add an observer to this signaling client.
     * @param obs The observer.
     */
    void addObserver(Observer obs);

    /**
     * Remove an observer from this signaling client.
     * @param obs The observer to be removed.
     */
    void removeObserver(Observer obs);
}
