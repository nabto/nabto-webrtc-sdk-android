package com.example.myapplication;

import android.util.Log;

import com.nabto.webrtc.util.MessageTransport;
import com.nabto.webrtc.util.SignalingIceServer;
import com.nabto.webrtc.util.WebRTCSignalingMessageUnion;
import com.nabto.webrtc.util.impl.SignalingMessageUnion;

import java.util.List;

public class LoggingMessageTransportObserverAdapter implements MessageTransport.Observer {
    final String TAG = "LoggingMessageTransportObserverAdapter";
    @Override
    public void onWebRTCSignalingMessage(WebRTCSignalingMessageUnion message) {
        Log.i(TAG, "onWebRTCSignalingMessage");
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "onError", error);
    }

    @Override
    public void onSetupDone(List<SignalingIceServer> iceServers) {
        Log.i(TAG, "onSetupDone");
    }
}
