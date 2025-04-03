package com.nabto.signaling;

/**
 * Interface representing a logical channel to the camera through the underlying websocket relay connection.
 */
public interface SignalingChannel extends AutoCloseable {

    /**
     * Observer interface for callbacks
     */
    interface Observer {
        /**
         * Callback invoked when a message is received from the Camera
         * @param message The received message
         */
        void onMessage(String message);

        /**
         * Callback invoked when the channel state changes
         * @param newState The new channel state
         */
        void onChannelStateChange(SignalingChannelState newState);

        /**
         * Callback invoked when the underlying signaling channel was reconnected.
         */
        void onSignalingReconnect();

        /**
         * Callback invoked if an error occurs on the signaling channel.
         *
         * The error can be triggered locally by the SDK, or remotely by the Camera sending an error message. All errors are fatal, so the channel should be closed when an error occurs.
         *
         * @param error The error that occurred.
         */
        void onSignalingError(SignalingError error);
    }

    /**
     * Returns the current state of this signaling channel.
     *
     * @return The current {@link SignalingChannelState}
     */
    SignalingChannelState getChannelState();

    /**
     * Send a message to the other peer
     * @param msg The message to send
     */
    void sendMessage(String msg);

    /**
     * Send an error to the other peer.
     * @param errorCode The error code
     * @param errorMessage A string message explaining the error
     */
    void sendError(String errorCode, String errorMessage);


    /**
     * Trigger the underlying SignalingClient to ping the backend to test that the connection is alive.
     *
     * If the connection is dead it will be reconnected. Any result is reported in the onSignalingReconnect() and onSignalingError() callbacks.
     */
    void checkAlive();

    /**
     * Add an observer to this signaling channel.
     * @param obs The observer.
     */
    void addObserver(Observer obs);

    /**
     * Remove an observer from this signaling channel.
     * @param obs The observer to be removed.
     * @return true if the observer was removed, false if the observer was not observing this channel.
     */
    boolean removeObserver(Observer obs);
}
