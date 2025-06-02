package com.nabto.webrtc.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RoutingMessageError {
    @NonNull final String errorCode;
    @NonNull public final String errorMessage;

    public RoutingMessageError(@NonNull String errorCode, @Nullable String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
