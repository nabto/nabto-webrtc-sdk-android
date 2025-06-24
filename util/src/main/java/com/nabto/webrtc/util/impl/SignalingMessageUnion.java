package com.nabto.webrtc.util.impl;

import androidx.annotation.NonNull;

import com.nabto.webrtc.util.SignalingCandidate;
import com.nabto.webrtc.util.SignalingDescription;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SignalingMessageUnion {
    private SignalingCandidate candidate = null;
    private SignalingSetupRequest setupRequest = null;
    private SignalingSetupResponse setupResponse = null;
    private SignalingDescription description = null;

    public static SignalingMessageUnion fromJson(JSONObject json) throws IOException {
        SignalingMessageUnion result = new SignalingMessageUnion();
        var message = json.toString();
        String type = "";
        try {
            type = json.getString("type");
        } catch (JSONException e) {
            throw new IOException(e);
        }

        switch (type) {
            case SignalingMessageType.CANDIDATE:
                result.setCandidate(JsonUtil.fromJson(SignalingCandidate.class, message));
                break;
            case SignalingMessageType.DESCRIPTION:
                result.setDescription(JsonUtil.fromJson(SignalingDescription.class, message));
                break;
            case SignalingMessageType.SETUP_REQUEST:
                result.setSetupRequest(JsonUtil.fromJson(SignalingSetupRequest.class, message));
                break;
            case SignalingMessageType.SETUP_RESPONSE:
                result.setSetupResponse(JsonUtil.fromJson(SignalingSetupResponse.class, message));
                break;
            default:
                throw new IOException("DefaultMessageEncoder::decodeMessage input argument is invalid! " + message + " is not a valid signaling message.");
        }

        return result;
    }

    public boolean isCandidate() {
        return candidate != null;
    }

    public boolean isSetupRequest() {
        return setupRequest != null;
    }

    public boolean isSetupResponse() {
        return setupResponse != null;
    }

    public boolean isDescription() {
        return description != null;
    }

    public SignalingCandidate getCandidate() {
        return candidate;
    }

    public SignalingSetupRequest getSetupRequest() {
        return setupRequest;
    }

    public SignalingSetupResponse getSetupResponse() {
        return setupResponse;
    }

    public SignalingDescription getDescription() {
        return description;
    }

    public void setCandidate(@NonNull SignalingCandidate candidate) {
        this.candidate = candidate;
    }

    public void setSetupRequest(@NonNull SignalingSetupRequest setupRequest) {
        this.setupRequest = setupRequest;
    }

    public void setSetupResponse(@NonNull SignalingSetupResponse setupResponse) {
        this.setupResponse = setupResponse;
    }

    public void setDescription(@NonNull SignalingDescription description) {
        this.description = description;
    }
}
