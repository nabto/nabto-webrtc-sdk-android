package com.nabto.webrtc.util;

import androidx.annotation.Nullable;

import com.nabto.webrtc.util.impl.JsonUtil;
import com.nabto.webrtc.util.impl.SignalingMessage;
import com.nabto.webrtc.util.impl.SignalingMessageType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * SignalingMessage class representing an ICE candidate sent through the message transport.
 */
public class SignalingCandidate implements SignalingMessage {
    public final String type = SignalingMessageType.CANDIDATE;

    /**
     * Field containing the information in the candidate.
     */
    public final Candidate candidate = new Candidate();

    /**
     * Candidate information class definition
     */
    public static class Candidate {
        /**
         * The string representation of the candidate
         */
        public String candidate = "";

        /**
         * Optional SDP MID of the candidate.
         */
        @Nullable public String sdpMid = null;
        // @TODO: Might be wrong to use the boxed Integer here?
        /**
         * Optional SDP M Line Index of the candidate.
         */
        @Nullable public Integer sdpMLineIndex = null;

        /**
         * Optional Username Fragment of the candidate. If this is empty, it
         * means the value does not exist.
         */
        @Nullable public String usernameFragment = null;

        public int getSdpMLineIndex() {
            // The org.webrtc.IceCandidate constructor needs an sdpMLineIndex, if we do not have one,
            // return 0, this could be a problem if the implementation is actually relying on the value.
            if (sdpMLineIndex == null) {
                return 0;
            }
            return sdpMLineIndex;
        }
    }

    /**
     * Construct a Candidate to be sent by the MessageTransport.
     *
     * @param candidate The string representation of the candidate.
     */
    public SignalingCandidate(String candidate) {
        this.candidate.candidate = candidate;
    }

    /**
     * Build the candidate with the optional SDP MID value.
     *
     * @param sdpMid The MID to set.
     */
    public SignalingCandidate withSdpMid(String sdpMid) {
        this.candidate.sdpMid = sdpMid;
        return this;
    }

    /**
     * Build the candidate with the optional SDP M Line Index value.
     *
     * @param sdpMLineIndex The index to set.
     */
    public SignalingCandidate withSdpMLineIndex(int sdpMLineIndex) {
        this.candidate.sdpMLineIndex = sdpMLineIndex;
        return this;
    }

    /**
     * Build the candidate with the optional username fragment value.
     *
     * @param usernameFragment The username fragment to set.
     */
    public SignalingCandidate withUsernameFragment(String usernameFragment) {
        this.candidate.usernameFragment = usernameFragment;
        return this;
    }

    /**
     * Convert the candidate to JSON.
     *
     * @return The resulting JSON object.
     */
    @Override
    public JSONObject toJson() {
        try {
            return new JSONObject(toJsonString());
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    /**
     * Convert the candidate to stringified JSON.
     *
     * @return The resulting JSON string.
     */
    @Override
    public String toJsonString() {
        return JsonUtil.toJson(SignalingCandidate.class, this);
    }

    /**
     * Create a candidate from a JSON string.
     *
     * @param json The JSON string to parse
     * @return The resulting Candidate object.
     */
    public static SignalingCandidate fromJson(String json) throws IOException {
        return JsonUtil.fromJson(SignalingCandidate.class, json);
    }
}
