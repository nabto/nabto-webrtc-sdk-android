package com.nabto.signaling.schema;

import androidx.annotation.Nullable;

import java.util.List;

public class IceServer {
    public final List<String> urls;
    @Nullable public String credential = null;
    @Nullable public String username = null;

    IceServer(List<String> urls) {
        this.urls = urls;
    }

    public IceServer withCredential(String credential) {
        this.credential = credential;
        return this;
    }

    public IceServer withUsername(String username) {
        this.username = username;
        return this;
    }
}
