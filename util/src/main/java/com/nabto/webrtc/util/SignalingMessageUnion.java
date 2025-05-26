package com.nabto.webrtc.util;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SignalingMessageUnion {
    private SignalingCandidate candidate = null;
    private SignalingSetupRequest createRequest = null;
    private SignalingSetupResponse createResponse = null;
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

    public boolean isCreateRequest() {
        return createRequest != null;
    }

    public boolean isCreateResponse() {
        return createResponse != null;
    }

    public boolean isDescription() {
        return description != null;
    }

    public SignalingCandidate getCandidate() {
        return candidate;
    }

    public SignalingSetupRequest getCreateRequest() {
        return createRequest;
    }

    public SignalingSetupResponse getCreateResponse() {
        return createResponse;
    }

    public SignalingDescription getDescription() {
        return description;
    }

    public void setCandidate(@NonNull SignalingCandidate candidate) {
        this.candidate = candidate;
    }

    public void setSetupRequest(@NonNull SignalingSetupRequest createRequest) {
        this.createRequest = createRequest;
    }

    public void setSetupResponse(@NonNull SignalingSetupResponse createResponse) {
        this.createResponse = createResponse;
    }

    public void setDescription(@NonNull SignalingDescription description) {
        this.description = description;
    }
}
