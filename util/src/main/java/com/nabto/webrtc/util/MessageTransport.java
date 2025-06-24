package com.nabto.webrtc.util;

import java.util.List;

public interface MessageTransport {
    enum Mode {
        CLIENT,
        DEVICE
    }

    Mode getMode();

    // @todo use Webrtc signaling message union
    void sendWebRTCSignalingMessage(WebRTCSignalingMessage message);

    public void addObserver(MessageTransport.Observer observer);
    public void removeObserver(MessageTransport.Observer observer);
    /**
     * Observer interface for callbacks
     */
    public interface Observer {
        /**
         * Callback invoked when a message is received from the Camera
         * @param message The received message
         */
        // @todo use Webrtc signaling message union
        void onWebRTCSignalingMessage(WebRTCSignalingMessageUnion message);

        /**
         * Callback invoked if an error occurs in the message transport.
         *
         * @param error The error that occurred.
         */
        void onError(Exception error);

        /**
         * Callback invoked when the setup phase of the message transport is concluded.
         */
        void onSetupDone(List<SignalingIceServer> iceServers);
    }

    class AbstractObserver implements Observer {

        @Override
        public void onWebRTCSignalingMessage(WebRTCSignalingMessageUnion message) {

        }

        @Override
        public void onError(Exception error) {

        }

        @Override
        public void onSetupDone(List<SignalingIceServer> iceServers) {

        }
    }
}
