package com.nabto.webrtc.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class SignalingSetupResponse implements SignalingMessage {
    public final String type = SignalingMessageType.SETUP_RESPONSE;
    public final List<SignalingIceServer> iceServers;

    public SignalingSetupResponse(List<SignalingIceServer> iceServers) {
        this.iceServers = iceServers;
    }

    @Override
    public JSONObject toJson() {
        try {
            return new JSONObject(toJsonString());
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    @Override
    public String toJsonString() {
        return JsonUtil.toJson(SignalingSetupResponse.class, this);
    }

    public static SignalingSetupResponse fromJson(String json) throws IOException {
        return JsonUtil.fromJson(SignalingSetupResponse.class, json);
    }
}
