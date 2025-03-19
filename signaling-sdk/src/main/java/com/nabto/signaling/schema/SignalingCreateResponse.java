package com.nabto.signaling.schema;

import java.io.IOException;
import java.util.List;

public class SignalingCreateResponse implements SignalingMessage {
    public final String type = SignalingMessageType.CREATE_RESPONSE;
    public final List<IceServer> iceServers;

    public SignalingCreateResponse(List<IceServer> iceServers) {
        this.iceServers = iceServers;
    }

    @Override
    public String toJson() {
        return JsonUtil.toJson(SignalingCreateResponse.class, this);
    }

    public static SignalingCreateResponse fromJson(String json) throws IOException {
        return JsonUtil.fromJson(SignalingCreateResponse.class, json);
    }
}
