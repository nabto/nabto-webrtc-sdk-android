package com.nabto.webrtc.util;

import java.util.List;

/**
 * The MessageTransport interface is used as a middleware to encode, validate,
 * sign, and verify messages sent and received on a Signaling Channel.
 *
 * The responsibilities of the Message Transport is to initially setup the
 * channel. When this is done, it is used to exchange WebRTC Signaling Messages
 * between the client and the device.
 *
 * The onSetupdone() event is fired when the channel is setup. The
 * PeerConnection should be created in this callback and it should be created
 * with the RTC ICE Servers provided in the callback.
 */
public interface MessageTransport extends AutoCloseable {
    // @todo use Webrtc signaling message union
    /**
     * Send a message through the MessageTransport and the signaling channel to the other peer.
     *
     * @param message The message to send.
     */
    void sendWebrtcSignalingMessage(WebrtcSignalingMessage message);

    /**
     * Add an observer to receive callbacks when events occurs.
     *
     * @param observer The observer to add.
     */
    public void addObserver(MessageTransport.Observer observer);

    /**
     * Remove an observer from the MessageTransport.
     *
     * @param observer The observer to remove.
     */
    public void removeObserver(MessageTransport.Observer observer);

    /**
     * Observer interface for callbacks
     */
    public interface Observer {
        // @todo use Webrtc signaling message union
        /**
         * Callback invoked when a message is received from the Camera.
         *
         * @param message The received message.
         */
        void onWebrtcSignalingMessage(WebrtcSignalingMessage message);

        /**
         * Callback invoked if an error occurs in the message transport.
         *
         * @param error The error that occurred.
         */
        void onError(Throwable error);

        /**
         * Callback invoked when the setup phase of the message transport is concluded.
         *
         * @param iceServers A list of ICE servers to use in Peer Connection.
         */
        void onSetupDone(List<SignalingIceServer> iceServers);
    }

    /**
     * Abstract Observer implementation with default implementations which can be extended.
     */
    class AbstractObserver implements Observer {

        @Override
        public void onWebrtcSignalingMessage(WebrtcSignalingMessage message) {

        }

        @Override
        public void onError(Throwable error) {

        }

        @Override
        public void onSetupDone(List<SignalingIceServer> iceServers) {

        }
    }
}
