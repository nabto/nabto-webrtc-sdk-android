package com.nabto.webrtc;

/**
 * Hint about the state of the remote peer. The state is only updated in
 * certain situations and in some cases it does not reflect the actual state
 * of the remote peer.
 */
public enum SignalingChannelState {
    /**
     * The state is new if the client has not been connected yet.
     */
    NEW,
    /**
     * The state is online if our best guess is that the device is currently online.
     */
    ONLINE,
    /**
     * The state is offline if the current best guess is that the device is offline.
     */
    OFFLINE,
    /**
     * If the channel has received an error which is fail in the protocol the state is failed.
     */
    FAILED,
    /**
     * If close has been called on the channel by the application or by the signaling client, the state is closed.
     */
    CLOSED
}
