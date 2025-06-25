package com.example.myapplication;

import android.util.Log;

import com.nabto.webrtc.util.MessageTransport;
import com.nabto.webrtc.util.SignalingIceServer;
import com.nabto.webrtc.util.WebrtcSignalingMessageUnion;

import java.util.List;

public class LoggingMessageTransportObserverAdapter implements MessageTransport.Observer {
    final String TAG = "LoggingMessageTransportObserverAdapter";
    @Override
    public void onWebrtcSignalingMessage(WebrtcSignalingMessageUnion message) {
        Log.i(TAG, "onWebrtcSignalingMessage");
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "onError: " + error.getMessage());
    }

    @Override
    public void onSetupDone(List<SignalingIceServer> iceServers) {
        Log.i(TAG, "onSetupDone");
    }
}
