package com.nabto.webrtc.impl;

public class DeviceOfflineException extends RuntimeException {

    public DeviceOfflineException() {
        super("The requested device is offline. But the requireOnline bit was set.");
    }
}
