package com.nabto.webrtc;

import com.nabto.webrtc.impl.SignalingClientImpl;

/**
 * {@link SignalingClientFactory} contains static utilities to create {@link SignalingClient} objects with.
 */
public class SignalingClientFactory {
    /**
     * {@link Options} is used by {@link #createSignalingClient(Options)}
     */
    public static class Options {
        private String endpointUrl = "";
        private String productId = "";
        private String deviceId = "";
        private String accessToken = "";
        private boolean requireOnline = false;

        /**
         * Set the URL for the Nabto Signaling Service that the SignalingClient will connect to.
         * @param endpointUrl Signaling service URL
         * @return This {@link Options} object.
         */
        public Options setEndpointUrl(String endpointUrl) {
            this.endpointUrl = endpointUrl;
            return this;
        }

        /**
         * Set the ID of the product that contains the device the SignalingClient should connect to.
         * @param productId ID of the product to connect to.
         * @return This {@link Options} object.
         */
        public Options setProductId(String productId) {
            this.productId = productId;
            return this;
        }

        /**
         * Set the ID of the device that the SignalingClient should connect to.
         * @param deviceId ID of the device to connect to.
         * @return This {@link Options} object.
         */
        public Options setDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        /**
         * Set whether the SignalingClient should require the device to be online.
         * @param requireOnline If true specifies that the device should be online.
         * @return This Options object.
         */
        public Options setRequireOnline(boolean requireOnline) {
            this.requireOnline = requireOnline;
            return this;
        }

        /**
         * Set an Access Token for the client to use when connecting to the Nabto WebRTC Signaling Service.
         * @param requireOnline If true specifies that the device should be online.
         * @return This Options object.
         */
        public Options setAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }
    }

    /**
     * Create a {@link SignalingClient} that can connect to a device over the signaling service.
     * @param opts An {@link Options} object.
     * @return An implementation of {@link SignalingClient}.
     */
    public static SignalingClient createSignalingClient(Options opts) {
        return new SignalingClientImpl(
                opts.endpointUrl,
                opts.productId,
                opts.deviceId,
                opts.requireOnline,
                opts.accessToken
        );
    }
}
