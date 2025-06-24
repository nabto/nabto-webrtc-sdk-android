package com.nabto.webrtc.util;

import com.nabto.webrtc.SignalingClient;
import com.nabto.webrtc.util.impl.ClientMessageTransportImpl;

public class ClientMessageTransport {
        /**
         * Create a Message transport which uses a Shared Secret Message Signer.
         * @param sharedSecret The shared secret to use in the message transport.
         * @param keyId The optional KeyID to use in the signed payloads.
         * @return A Message Transport.
         */
    public static MessageTransport createSharedSecretMessageTransport(SignalingClient client, String sharedSecret, String keyId) {
        return ClientMessageTransportImpl.createSharedSecretMessageTransport(client, sharedSecret, keyId);
    }
    /**
         * Create a Message transport which uses a Shared Secret Message Signer.
         * @param sharedSecret The shared secret to use in the message transport.
         * @return A Message Transport.
         */
    public static MessageTransport createSharedSecretMessageTransport(SignalingClient client, String sharedSecret) {
        return ClientMessageTransportImpl.createSharedSecretMessageTransport(client, sharedSecret, null);
    }

        /**
         * Create a message transport which does not implement message signing.
         * @return A Message Transport
         */
    public static MessageTransport createNoneMessageTransport(SignalingClient client) {
        return ClientMessageTransportImpl.createNoneMessageTransport(client);
    }
}
