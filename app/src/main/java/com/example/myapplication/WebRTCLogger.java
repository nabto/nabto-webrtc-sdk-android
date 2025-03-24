package com.example.myapplication;

import android.util.Log;

import org.webrtc.Loggable;
import org.webrtc.Logging;

public class WebRTCLogger implements Loggable {
    @Override
    public void onLogMessage(String unused, Logging.Severity severity, String msg) {
        Log.d("WebRTC", msg);
    }
}
