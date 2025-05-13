package com.nabto.webrtc.util;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;

public class SignalingIceServer {
    public final List<String> urls;
    @Nullable public String credential = null;
    @Nullable public String username = null;

    SignalingIceServer(List<String> urls) {
        this.urls = urls;
    }

    public SignalingIceServer withCredential(String credential) {
        this.credential = credential;
        return this;
    }

    public SignalingIceServer withUsername(String username) {
        this.username = username;
        return this;
    }

    public static SignalingIceServer fromJson(String json) throws IOException {
        return JsonUtil.fromJson(SignalingIceServer.class, json);
    }
}
