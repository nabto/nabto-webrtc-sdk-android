package com.nabto.webrtc.util;

import org.json.JSONObject;

public interface SignalingMessage {
    JSONObject toJson();
    String toJsonString();
}
