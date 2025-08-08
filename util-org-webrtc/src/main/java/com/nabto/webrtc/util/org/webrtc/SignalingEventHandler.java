package com.nabto.webrtc.util.org.webrtc;

import com.nabto.webrtc.SignalingChannelState;
import com.nabto.webrtc.SignalingClient;
import com.nabto.webrtc.SignalingConnectionState;

import org.json.JSONObject;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.PeerConnectionState;

/**
 * The purpose of this component is to handle signaling events such as signaling
 * reconnects. And react to RTCPeerConnection events which needs to trigger
 * signaling actions such as checkAlive and restartIce.
 * <p>
 * Use this class by adding it as an observer to your SignalingClient
 * and calling handlePeerConnectionStateChange from
 * your PeerConnection observer's onConnectionChange method.
 */
public class SignalingEventHandler extends SignalingClient.AbstractObserver {
    private final PeerConnection peerConnection;
    private final SignalingClient client;

    public SignalingEventHandler(PeerConnection peerConnection, SignalingClient client) {
        this.peerConnection = peerConnection;
        this.client = client;
    }

    void handlePeerConnectionStateChange() {
        if (peerConnection != null && client != null) {
            if (peerConnection.connectionState() == PeerConnectionState.DISCONNECTED) {
                client.checkAlive();
            }

            if (peerConnection.connectionState() == PeerConnectionState.FAILED) {
                peerConnection.restartIce();
            }
        }
    }

    @Override public void onConnectionReconnect() {
        if (peerConnection != null) {
            peerConnection.restartIce();
        }
    }
}
