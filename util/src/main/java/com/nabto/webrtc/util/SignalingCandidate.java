package com.nabto.webrtc.util;

import androidx.annotation.Nullable;

import com.nabto.webrtc.util.impl.JsonUtil;
import com.nabto.webrtc.util.impl.SignalingMessage;
import com.nabto.webrtc.util.impl.SignalingMessageType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SignalingCandidate implements SignalingMessage, WebRTCSignalingMessage {
    public final String type = SignalingMessageType.CANDIDATE;
    public final Candidate candidate = new Candidate();

    public static class Candidate {
        public String candidate = "";
        @Nullable public String sdpMid = null;
        // @TODO: Might be wrong to use the boxed Integer here?
        @Nullable public Integer sdpMLineIndex = null;
        @Nullable public String usernameFragment = null;
    }

    public SignalingCandidate(String candidate) {
        this.candidate.candidate = candidate;
    }

    public SignalingCandidate withSdpMid(String sdpMid) {
        this.candidate.sdpMid = sdpMid;
        return this;
    }

    public SignalingCandidate withSdpMLineIndex(int sdpMLineIndex) {
        this.candidate.sdpMLineIndex = sdpMLineIndex;
        return this;
    }

    public SignalingCandidate withUsernameFragment(String usernameFragment) {
        this.candidate.usernameFragment = usernameFragment;
        return this;
    }

    @Override
    public JSONObject toJson() {
        try {
            return new JSONObject(toJsonString());
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    @Override
    public String toJsonString() {
        return JsonUtil.toJson(SignalingCandidate.class, this);
    }

    public static SignalingCandidate fromJson(String json) throws IOException {
        return JsonUtil.fromJson(SignalingCandidate.class, json);
    }
}
