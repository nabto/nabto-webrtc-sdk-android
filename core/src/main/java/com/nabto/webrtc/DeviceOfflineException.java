package com.nabto.webrtc;

public class DeviceOfflineException extends RuntimeException {

    /**
     * Thrown if the device is offline but was required to be online while connecting to the
     * Nabto WebRTC signaling Service.
     */
    public DeviceOfflineException() {
        super("The requested device is offline. But the requireOnline bit was set.");
    }
}
