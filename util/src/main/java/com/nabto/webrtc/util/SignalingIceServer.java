package com.nabto.webrtc.util;

import androidx.annotation.Nullable;

import com.nabto.webrtc.util.impl.JsonUtil;

import java.io.IOException;
import java.util.List;

/**
 * Class representing an ICE server returned by the Nabto Backend.
 */
public class SignalingIceServer {
    /**
     * List of URLs for the ICE server. If the server is a TURN server, the
     * credentials will be valid for all URLs in the list.
     */
    public final List<String> urls;

    /**
     * credential will be null if the server is a STUN server, and a
     * credential if it is a TURN server.
     */
    @Nullable public String credential = null;

    /**
     * username will be null if the server is a STUN server, and a
     * username if it is a TURN server.
     */
    @Nullable public String username = null;

    /**
     * Construct an ICE server object.
     *
     * @param urls List of URLs for this ICE server.
     */
    SignalingIceServer(List<String> urls) {
        this.urls = urls;
    }

    /**
     * Build the ICE server with credential.
     *
     * @param credential The credential to set.
     * @return *this reference.
     */
    public SignalingIceServer withCredential(String credential) {
        this.credential = credential;
        return this;
    }

    /**
     * Build the ICE server with username.
     *
     * @param credential The username to set.
     * @return *this reference.
     */
    public SignalingIceServer withUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * Build an ICE server from a JSON string.
     *
     * @param json The JSON string to parse.
     * @return The created ICE server object.
     */
    public static SignalingIceServer fromJson(String json) throws IOException {
        return JsonUtil.fromJson(SignalingIceServer.class, json);
    }
}
