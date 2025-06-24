package com.nabto.webrtc.util.impl;

import org.json.JSONObject;

public interface SignalingMessage {
    JSONObject toJson();
    String toJsonString();
}
