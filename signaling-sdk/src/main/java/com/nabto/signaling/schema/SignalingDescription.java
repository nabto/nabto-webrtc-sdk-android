package com.nabto.signaling.schema;

import java.io.IOException;

public class SignalingDescription implements SignalingMessage {
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
    public String toJson() {
        return JsonUtil.toJson(SignalingDescription.class, this);
    }

    public static SignalingDescription fromJson(String json) throws IOException {
        return JsonUtil.fromJson(SignalingDescription.class, json);
    }
}
