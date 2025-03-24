package com.nabto.signaling.schema;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SignalingMessageUnion {
    private SignalingCandidate candidate = null;
    private SignalingCreateRequest createRequest = null;
    private SignalingCreateResponse createResponse = null;
    private SignalingDescription description = null;

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
