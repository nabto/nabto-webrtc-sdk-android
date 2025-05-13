package com.nabto.webrtc.util;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SignalingMessageUnion {
    private SignalingCandidate candidate = null;
    private SignalingCreateRequest createRequest = null;
    private SignalingCreateResponse createResponse = null;
    private SignalingDescription description = null;

    public static SignalingMessageUnion fromJson(String message) throws IOException {
        SignalingMessageUnion result = new SignalingMessageUnion();
        String type = "";
        try {
            JSONObject json = new JSONObject(message);
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
            case SignalingMessageType.CREATE_REQUEST:
                result.setCreateRequest(JsonUtil.fromJson(SignalingCreateRequest.class, message));
                break;
            case SignalingMessageType.CREATE_RESPONSE:
                result.setCreateResponse(JsonUtil.fromJson(SignalingCreateResponse.class, message));
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

    public SignalingCreateRequest getCreateRequest() {
        return createRequest;
    }

    public SignalingCreateResponse getCreateResponse() {
        return createResponse;
    }

    public SignalingDescription getDescription() {
        return description;
    }

    public void setCandidate(@NonNull SignalingCandidate candidate) {
        this.candidate = candidate;
    }

    public void setCreateRequest(@NonNull SignalingCreateRequest createRequest) {
        this.createRequest = createRequest;
    }

    public void setCreateResponse(@NonNull SignalingCreateResponse createResponse) {
        this.createResponse = createResponse;
    }

    public void setDescription(@NonNull SignalingDescription description) {
        this.description = description;
    }
}
