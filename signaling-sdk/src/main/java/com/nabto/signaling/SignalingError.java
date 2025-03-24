package com.nabto.signaling;

public class SignalingError extends RuntimeException {
    public static final String DECODE_ERROR = "DECODE_ERROR";
    public static final String VERIFICATION_ERROR = "VERIFICATION_ERROR";
    public static final String CHANNEL_CLOSED = "CHANNEL_CLOSED";
    public static final String CHANNEL_NOT_FOUND = "CHANNEL_NOT_FOUND";
    public static final String NO_MORE_CHANNELS = "NO_MORE_CHANNELS";

    public String errorCode;
    public String errorMessage;
    public boolean isRemote;

    public SignalingError(String errorCode, String errorMessage, boolean isRemote) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.isRemote = isRemote;
    }

    public SignalingError(String errorCode, String errorMessage) {
        this(errorCode, errorMessage, false);
    }
}
