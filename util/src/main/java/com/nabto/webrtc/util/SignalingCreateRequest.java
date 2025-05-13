package com.nabto.webrtc.util;

import java.io.IOException;

public class SignalingCreateRequest implements SignalingMessage {
    public final String type = SignalingMessageType.CREATE_REQUEST;

    @Override
    public String toJson() {
        return JsonUtil.toJson(SignalingCreateRequest.class, this);
    }

    public static SignalingCreateRequest fromJson(String json) throws IOException {
        return JsonUtil.fromJson(SignalingCreateRequest.class, json);
    }
}
