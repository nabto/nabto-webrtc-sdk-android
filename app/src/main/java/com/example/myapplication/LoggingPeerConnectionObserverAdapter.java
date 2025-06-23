package com.example.myapplication;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;

public abstract class LoggingPeerConnectionObserverAdapter implements PeerConnection.Observer {
    final String TAG = "LoggingPeerConnectionObserverAdapter";
    @Override public void onSignalingChange(PeerConnection.SignalingState newState) {
        Log.i(TAG, "onSignalingChange newState: " + newState.name());
    }
    @Override public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {
        Log.i(TAG, "onIceConnectionChange newState: " + newState.name());
    }
    @Override public void onIceConnectionReceivingChange(boolean receiving) {
        Log.i(TAG, "onIceConnectionReceivingChange receiving: " + receiving);
    }
    @Override public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
        Log.i(TAG, "onIceGatheringChange newState: " + newState.name());
    }
    @Override public void onIceCandidate(IceCandidate candidate) {
        Log.i(TAG, "onIceCandidate candidate: " + candidate);
    }
    @Override public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        Log.i(TAG, "onIceCandidatesRemoved candidates length: " + candidates.length);
    }
    @Override public void onAddStream(MediaStream stream) {
        Log.i(TAG, "onAddStream stream: " + stream);
    }
    @Override public void onRemoveStream(MediaStream stream) {
        Log.i(TAG, "onRemoveStream stream: " + stream);
    }
    @Override public void onDataChannel(DataChannel dc) {
        Log.i(TAG, "onDataChannel dataChannel: " + dc);
    }
    @Override public void onRenegotiationNeeded() {
        Log.i(TAG, "onRenegotiationNeeeded");
    }
    @Override public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {
        Log.i(TAG, "onAddTrack");
    }
}