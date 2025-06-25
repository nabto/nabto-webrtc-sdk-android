package com.nabto.webrtc;

/**
 * The different states the Signaling Connection can have.
 *
 *  - NEW: The Signaling Connection was just created
 *  - CONNECTING: The Signaling Connection is connecting to the backend.
 *  - CONNECTED: The Signaling Connection is connected and ready to use.
 *  - WAIT_RETRY: The Signaling Connection is disconnected and waiting for its backoff before reconnecting.
 *  - FAILED: The Signaling Connection has failed and will not reconnect.
 *  - CLOSED: The Signaling Connection was closed by the application.
 */
public enum SignalingConnectionState {
    NEW,
    CONNECTING,
    CONNECTED,
    WAIT_RETRY,
    FAILED,
    CLOSED
}
