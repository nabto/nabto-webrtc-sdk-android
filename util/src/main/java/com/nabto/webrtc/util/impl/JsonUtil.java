package com.nabto.webrtc.util.impl;

import com.squareup.moshi.Moshi;

import java.io.IOException;

public class JsonUtil {
    private static final Moshi moshi = new Moshi.Builder().build();

    public static <T> String toJson(Class<T> cls, T obj) {
        return moshi.adapter(cls).toJson(obj);
    }

    public static <T> T fromJson(Class<T> cls, String json) throws IOException {
        return moshi.adapter(cls).fromJson(json);
    }
}
