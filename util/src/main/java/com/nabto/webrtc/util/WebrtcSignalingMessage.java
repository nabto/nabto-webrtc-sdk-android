package com.nabto.webrtc.util;

public class WebrtcSignalingMessage {
    private SignalingCandidate candidate = null;
    private SignalingDescription description = null;

    public WebrtcSignalingMessage(SignalingCandidate candidate) {
        this.candidate = candidate;
    }
    public WebrtcSignalingMessage(SignalingDescription description) {
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
