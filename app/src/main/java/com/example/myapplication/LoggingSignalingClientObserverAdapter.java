package com.example.myapplication;

import android.util.Log;

import com.nabto.webrtc.SignalingChannelState;
import com.nabto.webrtc.SignalingClient;
import com.nabto.webrtc.SignalingConnectionState;

import org.json.JSONObject;

public class LoggingSignalingClientObserverAdapter implements SignalingClient.Observer {
    final String TAG = "LoggingSignalingClientObserverAdapter";
    @Override
    public void onConnectionStateChange(SignalingConnectionState newState) {
        Log.i(TAG, "onConnectionStateChange newState: " + newState);
    }

    @Override
    public void onMessage(JSONObject message) {
        Log.i(TAG, "onMessage message: " + message);
    }

    @Override
    public void onChannelStateChange(SignalingChannelState newState) {
        Log.i(TAG, "onChannelStateChange newState: " + newState);
    }

    @Override
    public void onConnectionReconnect() {
        Log.i(TAG, "onConnectionReconnect");
    }

    @Override
    public void onError(Throwable error) {
        Log.e(TAG, "onError", error);
    }
}
