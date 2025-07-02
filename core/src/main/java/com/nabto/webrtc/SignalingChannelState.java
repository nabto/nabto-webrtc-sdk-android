package com.nabto.webrtc;

/**
 * Hint about the state of the remote peer. The state is only updated in
 * certain situations and in some cases it does not reflect the actual state
 * of the remote peer.
 *
 *  - NEW: The state is new if the client has not been connected yet.
 *  - CONNECTED: The state is connected if our best guess is that the other peer
 * is currently connected to the signaling service.
 *  - DISCONNECTED: The state is disconnected if the current best guess is that
 * the other peer is disconnected from the signaling service.
 *  - FAILED: If the channel has received an error which is fail in the protocol
 * the state is failed.
 *  - CLOSED: If close has been called on the channel by the application or by
 * the signaling client, the state is closed.
 */
public enum SignalingChannelState {
    NEW,
    CONNECTED,
    DISCONNECTED,
    FAILED,
    CLOSED
}
