package com.nabto.webrtc;

public class HttpException extends RuntimeException {
    /**
     *
     */
    public int statusCode;

    /**
     * This an encapsulation of Http Errors which can come from invoking the Nabto WebRTC Signaling Service.
     *
     * @param statusCode the http status code of the error.
     * @param message the messsga describing the error.
     */
    public HttpException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}

