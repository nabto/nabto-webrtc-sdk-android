package com.nabto.webrtc.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SignalingSetupRequest implements SignalingMessage {
    public final String type = SignalingMessageType.SETUP_REQUEST;

    @Override
    public JSONObject toJson() {
        var result = new JSONObject();
        try {
            result.put("type", type);
        } catch (JSONException e) {
            // @TODO: Logging
        }
        return result;
    }

    @Override
    public String toJsonString() {
        return JsonUtil.toJson(SignalingSetupRequest.class, this);
    }

    public static SignalingSetupRequest fromJson(String json) throws IOException {
        return JsonUtil.fromJson(SignalingSetupRequest.class, json);
    }
}
