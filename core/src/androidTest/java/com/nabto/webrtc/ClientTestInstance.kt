package com.nabto.webrtc

import android.util.Log
import kotlinx.coroutines.channels.Channel
import org.openapitools.client.apis.DefaultApi
import org.openapitools.client.models.PostTestClientRequest
import org.openapitools.client.models.PostTestClient200Response

val endpointUrl =  "http://192.168.1.128:13745";

public fun createClientTestInstance(): ClientTestInstance {
    var api = DefaultApi(endpointUrl);
    var testClient = api.postTestClient(PostTestClientRequest(endpointUrl = endpointUrl));
    return ClientTestInstance(testClient)

}

public class ClientTestInstance(private val config: PostTestClient200Response) {
    val observedConnectionStates = mutableListOf<SignalingConnectionState>(); // List<SignalingConnectionState>();
    private val stateChannel = Channel<SignalingConnectionState>(Channel.UNLIMITED)
    public fun createSignalingClient() : SignalingClient {
        val client = SignalingClientFactory.createSignalingClient(SignalingClientFactory.Options( ).setProductId(this.config.productId).setDeviceId(this.config.deviceId).setEndpointUrl(
            this.config.endpointUrl));
        client.addObserver(object: SignalingClient.AbstractObserver() {
            override fun onConnectionStateChange(newState: SignalingConnectionState) {
                observedConnectionStates.add(newState);
                stateChannel.trySend(newState);
            }
        });
        return client;
    }

    public suspend fun waitConnectionStates(states: List<SignalingConnectionState>) {
        if (observedConnectionStates == states) {
            return;
        }
        // else wait for updates until the desired states has been reached.
        for (state in stateChannel) {
            if (observedConnectionStates == states) {
               return;
            }
        }
    }
}
