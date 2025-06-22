package com.nabto.webrtc

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import org.junit.Test
import org.openapitools.client.apis.DefaultApi
import org.openapitools.client.models.PostTestClientRequest
import org.openapitools.client.models.PostTestClient200Response
import org.openapitools.client.models.PostTestClientByTestIdSendDeviceMessagesRequest
import org.openapitools.client.models.PostTestClientByTestIdWaitForDeviceMessagesRequest
import java.math.BigDecimal
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

val endpointUrl = BuildConfig.INTEGRATION_TEST_SERVER_URL;

val TAG : String = "ClientTestInstance";

data class ClientTestInstanceOptions(
    val failHttp: Boolean = false,
    val failWs: Boolean = false,
    val extraClientConnectResponseData: Boolean = false
) {
}

data class TestObject(val foo : String = "test") {
}

public fun createClientTestInstance(options: ClientTestInstanceOptions = ClientTestInstanceOptions()): ClientTestInstance {
    var api = DefaultApi(endpointUrl);

    var testClient = api.postTestClient(PostTestClientRequest(endpointUrl = endpointUrl, failWs = options.failWs, failHttp = options.failHttp, extraClientConnectResponseData = options.extraClientConnectResponseData ));
    return ClientTestInstance(testClient)

}

public class ClientTestInstance(private val config: PostTestClient200Response) : AutoCloseable {
    var api = DefaultApi(endpointUrl);
    val observedConnectionStates = mutableListOf<SignalingConnectionState>(); // List<SignalingConnectionState>();
    val observedErrors = mutableListOf<Throwable>(); // List<SignalingConnectionState>();
    val receviedMessages = mutableListOf<JSONObject>();
    private val stateChannel = Channel<SignalingConnectionState>(Channel.UNLIMITED)
    private val messageChannel = Channel<JSONObject>(Channel.UNLIMITED)
    @OptIn(ExperimentalStdlibApi::class)
    public fun createSignalingClient() : SignalingClient {
        val client = SignalingClientFactory.createSignalingClient(SignalingClientFactory.Options( ).setProductId(this.config.productId).setDeviceId(this.config.deviceId).setEndpointUrl(
            this.config.endpointUrl));
        client.addObserver(object: SignalingClient.AbstractObserver() {
            override fun onConnectionStateChange(newState: SignalingConnectionState) {
                observedConnectionStates.add(newState);
                stateChannel.trySend(newState);
            }

            override fun onError(error: Throwable) {
                Log.e(TAG, error.toString());
                observedErrors.add(error);
            }

            override fun onMessage(message: JSONObject) {
                messageChannel.trySend(message);
                receviedMessages.add(message);
            }
        });
        return client;
    }

    public suspend fun closeWebsocket() {
        api.postTestClientByTestIdDisconnectClient(this.config.testId);
    }
    public suspend fun sendUnknwonWebsocketMessageType() {
        api.postTestClientByTestIdSendNewMessageType(this.config.testId, Object())
    }
    public suspend fun sendNewFieldInKnownMessageType() {
        api.postTestClientByTestIdSendNewFieldInKnownMessageType(this.config.testId, Object())
    }

    public suspend fun sendMessageToClient(messages: List<Any>) {
        api.postTestClientByTestIdSendDeviceMessages(this.config.testId, PostTestClientByTestIdSendDeviceMessagesRequest(messages = messages))
    }

    public suspend fun connectDevice() {
        api.postTestClientByTestIdConnectDevice(this.config.testId);
    }
    public suspend fun disconnectDevice() {
        api.postTestClientByTestIdDisconnectDevice(this.config.testId);
    }

    public suspend fun dropClientMessages() {
        api.postTestClientByTestIdDropClientMessages(this.config.testId);
    }
    public suspend fun dropDeviceMessages() {
        api.postTestClientByTestIdDropDeviceMessages(this.config.testId);
    }
    public suspend fun getActiveWebSockets() : Number {
        val response = api.postTestClientByTestIdGetActiveWebsockets(this.config.testId, Object());
        return response.activeWebSockets;
    }

    public suspend fun waitConnectionStates(states: List<SignalingConnectionState>, timeoutMillis: Long = 10000) {
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

    @OptIn(ExperimentalStdlibApi::class)
    fun isMessagesEqual(generic: JSONObject, rhs: TestObject) : Boolean {
        val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<TestObject> = moshi.adapter<TestObject>();
        val lhs = jsonAdapter.fromJson(generic.toString());
        return lhs == rhs;
    }

    fun isMessageListsEqual(generic: List<JSONObject>, rhs: List<TestObject>) : Boolean {
        if (generic.size != rhs.size) return false

        return generic.zip(rhs).all { (json, obj) ->
            isMessagesEqual(json, obj)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    public suspend fun waitReceivedMessages(messages: List<TestObject>, timeoutMillis: Long = 10000) {
        if (isMessageListsEqual(receviedMessages, messages)) {
            return;
        }
        // else wait for updates until the desired states has been reached.
        try {
            withTimeout(timeoutMillis) {
                for (message in messageChannel) {
                    if (isMessageListsEqual(receviedMessages, messages)) {
                        return@withTimeout
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            throw AssertionError(
                "Timeout after ${timeoutMillis}ms waiting for messages. " +
                        "Expected: $messages, Observed: $receviedMessages", e
            )
        }
    }

    public suspend fun waitForDeviceToReceiveMessages(messages: List<TestObject>, timeoutMillis: Double) {
        api.postTestClientByTestIdWaitForDeviceMessages(this.config.testId, PostTestClientByTestIdWaitForDeviceMessagesRequest(messages = messages, timeout = timeoutMillis ))
    }

    override fun close() {
        api.deleteTestClientByTestId(this.config.testId);
    }
}
