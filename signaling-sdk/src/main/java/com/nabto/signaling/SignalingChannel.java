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
}
