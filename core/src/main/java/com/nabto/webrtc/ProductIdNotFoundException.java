package com.nabto.webrtc;

public class ProductIdNotFoundException extends HttpException {
    /**
     * Thrown if the product id is not found in the Nabto WebRTC Signaling Service.
     *
     * @param statusCode the http status code.
     * @param message the friendly message describing the error.
     */
    public ProductIdNotFoundException(int statusCode, String message) {
        super(statusCode, message);
    }
}