package com.nabto.webrtc;

/**
 * The different states the Signaling Connection can have.
 */
public enum SignalingConnectionState {
    /**
     * The Signaling Connection was just created
     */
    NEW,
    /**
     * The Signaling Connection is connecting to the backend.
     */
    CONNECTING,
    /**
     * The Signaling Connection is connected and ready to use.
     */
    CONNECTED,
    /**
     * The Signaling Connection is disconnected and waiting for its backoff before reconnecting.
     */
    WAIT_RETRY,
    /**
     * The Signaling Connection has failed and will not reconnect.
     */
    FAILED,
    /**
     * The Signaling Connection was closed by the application.
     */
    CLOSED
}
