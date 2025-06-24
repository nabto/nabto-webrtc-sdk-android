package com.nabto.webrtc.util;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class WebRTCSignalingMessageUnion {
    private SignalingCandidate candidate = null;
    private SignalingDescription description = null;

    public WebRTCSignalingMessageUnion(SignalingCandidate candidate) {
        this.candidate = candidate;
    }
    public WebRTCSignalingMessageUnion(SignalingDescription description) {
        this.description = description;
    }

    public boolean isCandidate() {
        return candidate != null;
    }

    public boolean isDescription() {
        return description != null;
    }

    public SignalingCandidate getCandidate() {
        return candidate;
    }

    public SignalingDescription getDescription() {
        return description;
    }
}
