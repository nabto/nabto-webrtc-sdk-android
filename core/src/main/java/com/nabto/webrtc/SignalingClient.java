package com.nabto.webrtc;

import org.json.JSONObject;

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

        /**
         * Callback invoked when a message is received from the Camera
         * Message are received sequentially and a blocking operation blocks further message deliveries and the underlying WebSocket.
         * @param message The received message
         */
        void onMessage(JSONObject message);

        /**
         * Callback invoked when the channel state changes
         * @param newState The new channel state
         */
        void onChannelStateChange(SignalingChannelState newState);

        /**
         * Callback invoked when the underlying signaling channel was reconnected.
         *
         */
        void onConnectionReconnect();

        /**
         * Callback invoked if an error occurs on the signaling channel.
         *
         * The error can be triggered locally by the SDK, or remotely by the Camera sending an error message. All errors are fatal, so the channel should be closed when an error occurs.
         *
         * @param error The error that occurred.
         *
         */
        void onError(Throwable error);
    }

    class AbstractObserver implements Observer {

        @Override
        public void onConnectionStateChange(SignalingConnectionState newState) {

        }

        @Override
        public void onMessage(JSONObject message) {

        }

        @Override
        public void onChannelStateChange(SignalingChannelState newState) {

        }

        @Override
        public void onConnectionReconnect() {

        }

        @Override
        public void onError(Throwable error) {

        }
    }

    /**
     * Start the signaling client, if an error occurs onError is called and the state is set to FAILED.
     */
    void start();

    /**
     * Get the current state of the Signaling Connection
     * @return The current state of the connection
     */
    SignalingConnectionState getConnectionState();

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
    void sendMessage(JSONObject msg);

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
