package com.nabto.webrtc.util;

import com.nabto.webrtc.util.impl.JsonUtil;
import com.nabto.webrtc.util.impl.SignalingMessage;
import com.nabto.webrtc.util.impl.SignalingMessageType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SignalingDescription implements SignalingMessage, WebrtcSignalingMessage {
    public final String type = SignalingMessageType.DESCRIPTION;
    public final Description description = new Description();

    public static class Description {
        public String type;
        public String sdp;
    }

    public SignalingDescription(String type, String sdp) {
        this.description.type = type;
        this.description.sdp = sdp;
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
        return JsonUtil.toJson(SignalingDescription.class, this);
    }

    public static SignalingDescription fromJson(String json) throws IOException {
        return JsonUtil.fromJson(SignalingDescription.class, json);
    }
}
