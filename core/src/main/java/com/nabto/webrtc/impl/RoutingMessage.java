package com.nabto.webrtc.impl;

import com.nabto.webrtc.SignalingError;

import org.json.JSONObject;

public class RoutingMessage {
    public RoutingMessageType type;
    public String channelId;
    public JSONObject message;
    public boolean authorized;
    public SignalingError signalingError;

    public RoutingMessage(RoutingMessageType type, String channelId, JSONObject message, boolean authorized, SignalingError signalingError) {
        this.type = type;
        this.channelId = channelId;
        this.message = message;
        this.authorized = authorized;
        this.signalingError = signalingError;
    }
}
