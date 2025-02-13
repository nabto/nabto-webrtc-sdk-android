package com.nabto.signaling;

/**
 * @TODO: Documentation
 */
public interface SignalingChannel extends AutoCloseable {
    /**
     * Returns the current state of this signaling channel.
     * @return {@link SignalingChannelState}
     */
    SignalingChannelState getChannelState();

    /**
     * Send a message to the other peer
     * @param msg
     */
    void sendMessage(String msg);

    /**
     * Send an error to the other peer.
     * @param errorCode The error code
     * @param errorMessage A string message explaining the error
     */
    void sendError(String errorCode, String errorMessage);

    // @TODO: checkAlive
    // @TODO: channelstatechange
    // @TODO: signalingreconnect
    // @TODO: signalingerror
}
