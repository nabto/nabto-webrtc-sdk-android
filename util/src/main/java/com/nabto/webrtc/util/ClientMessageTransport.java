package com.nabto.webrtc.util;

import com.nabto.webrtc.SignalingChannelState;
import com.nabto.webrtc.SignalingClient;
import com.nabto.webrtc.SignalingConnectionState;
import com.nabto.webrtc.SignalingError;
import com.nabto.webrtc.util.impl.ClientMessageTransportImpl;

import org.json.JSONObject;

import java.util.List;
import java.util.Optional;


public class ClientMessageTransport {
        /**
         * Create a Message transport which uses a Shared Secret Message Signer.
         * @param sharedSecret
         * @param keyId
         * @return
         */
    public static MessageTransport createSharedSecretMessageTransport(SignalingClient client, String sharedSecret, Optional<String> keyId) {
        return ClientMessageTransportImpl.createSharedSecretMessageTransport(client, sharedSecret, keyId);
    }

        /**
         * Create a message transport which does not implement message signing.
         * @return
         */
    public static MessageTransport createNoneMessageTransport(SignalingClient client) {
        return ClientMessageTransportImpl.createNoneMessageTransport(client);
    }
}
