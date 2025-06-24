package com.nabto.webrtc.util;

public class WebrtcSignalingMessageUnion {
    private SignalingCandidate candidate = null;
    private SignalingDescription description = null;

    public WebrtcSignalingMessageUnion(SignalingCandidate candidate) {
        this.candidate = candidate;
    }
    public WebrtcSignalingMessageUnion(SignalingDescription description) {
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
