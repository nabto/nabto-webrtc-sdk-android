package com.nabto.signaling;

public enum SignalingConnectionState {
    NEW,
    CONNECTING,
    CONNECTED,
    WAIT_RETRY,
    FAILED,
    CLOSED
}
