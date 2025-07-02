package com.nabto.webrtc.util;

/**
 * Generalized WebRTC Signaling message to be sent/received by the
 * MessageTransport. This message can contain either a SignalingDescription or a
 * SignalingCandidate.
 */
public class WebrtcSignalingMessage {
    private SignalingCandidate candidate = null;
    private SignalingDescription description = null;

    /**
     * Construct a WebRTC Signaling message from a SignalingCandidate.
     *
     * @param candidate The candidate to construct with.
     */
    public WebrtcSignalingMessage(SignalingCandidate candidate) {
        this.candidate = candidate;
    }

    /**
     * Construct a WebRTC Signaling message from a SignalingDescription.
     *
     * @param description The description to construct with.
     */
    public WebrtcSignalingMessage(SignalingDescription description) {
        this.description = description;
    }

    /**
     * Check if the message is a SignalingCandidate.
     *
     * @return True iff the message contains a candidate.
     */
    public boolean isCandidate() {
        return candidate != null;
    }

    /**
     * Check if the message is a SignalingDescription.
     *
     * @return True iff the message contains a description.
     */
    public boolean isDescription() {
        return description != null;
    }

    /**
     * Get the SignalingCandidate contained in this message if `isCandidate()`
     * returns true.
     *
     * @return The contained SignalingCandidate object.
     */
    public SignalingCandidate getCandidate() {
        return candidate;
    }

    /**
     * Get the SignalingDescription contained in this message if
     * `isDescription()` returns true.
     *
     * @return The contained SignalingDescription object.
     */
    public SignalingDescription getDescription() {
        return description;
    }
}
