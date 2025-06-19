package com.nabto.webrtc

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout
import org.openapitools.client.apis.DefaultApi
import org.openapitools.client.models.PostTestClientRequest
import org.openapitools.client.models.PostTestClient200Response

val endpointUrl = BuildConfig.INTEGRATION_TEST_SERVER_URL;

data class ClientTestInstanceOptions(
    val failHttp: Boolean = false,
    val failWs: Boolean = false
) {
}

public fun createClientTestInstance(options: ClientTestInstanceOptions = ClientTestInstanceOptions()): ClientTestInstance {
    var api = DefaultApi(endpointUrl);

    var testClient = api.postTestClient(PostTestClientRequest(endpointUrl = endpointUrl, failWs = options.failWs, failHttp = options.failHttp));
    return ClientTestInstance(testClient)

}

public class ClientTestInstance(private val config: PostTestClient200Response) : AutoCloseable {
    var api = DefaultApi(endpointUrl);
    val observedConnectionStates = mutableListOf<SignalingConnectionState>(); // List<SignalingConnectionState>();
    val observedErrors = mutableListOf<Throwable>(); // List<SignalingConnectionState>();
    private val stateChannel = Channel<SignalingConnectionState>(Channel.UNLIMITED)
    public fun createSignalingClient() : SignalingClient {
        val client = SignalingClientFactory.createSignalingClient(SignalingClientFactory.Options( ).setProductId(this.config.productId).setDeviceId(this.config.deviceId).setEndpointUrl(
            this.config.endpointUrl));
        client.addObserver(object: SignalingClient.AbstractObserver() {
            override fun onConnectionStateChange(newState: SignalingConnectionState) {
                observedConnectionStates.add(newState);
                stateChannel.trySend(newState);
            }

            override fun onError(error: Throwable) {
                observedErrors.add(error);
            }
        });
        return client;
    }

    public suspend fun closeWebsocket() {
        api.postTestClientByTestIdDisconnectClient(this.config.testId);
    }

    public suspend fun waitConnectionStates(states: List<SignalingConnectionState>, timeoutMillis: Long = 1000) {
        if (observedConnectionStates == states) {
            return;
        }
        // else wait for updates until the desired states has been reached.
        try {
            withTimeout(timeoutMillis) {
                for (state in stateChannel) {
                    if (observedConnectionStates == states) {
                        return@withTimeout;
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            throw AssertionError(
                "Timeout after ${timeoutMillis}ms waiting for connection states. " +
                        "Expected: $states, Observed: $observedConnectionStates", e
            )
        }
    }

    override fun close() {
        api.deleteTestClientByTestId(this.config.testId);
    }
}
