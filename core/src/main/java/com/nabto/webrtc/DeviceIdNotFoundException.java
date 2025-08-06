package com.nabto.webrtc;

public class DeviceIdNotFoundException extends HttpException {
    /**
     * Thrown if the devuce id is not found in the Nabto WebRTC Signaling Service.
     *
     * @param statusCode the http status code.
     * @param message the friendly message describing the error.
     */
    public DeviceIdNotFoundException(int statusCode, String message) {
        super(statusCode, message);
    }
}