package com.nabto.signaling;

/**
 * @TODO: Documentation
 */
public interface SignalingChannel extends AutoCloseable {
    interface Observer {
        void onMessage(String message);
        void onChannelStateChange(SignalingChannelState newState);
        void onSignalingReconnect();
        void onSignalingError(SignalingError error);
    }

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


    /**
     * @TODO: Documentation
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
