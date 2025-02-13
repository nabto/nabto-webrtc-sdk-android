package com.nabto.signaling;

public class SignalingError {
    public String errorCode;
    public String errorMessage;
    public boolean isRemote;

    public SignalingError(String errorCode, String errorMessage, boolean isRemote) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.isRemote = isRemote;
    }
}
