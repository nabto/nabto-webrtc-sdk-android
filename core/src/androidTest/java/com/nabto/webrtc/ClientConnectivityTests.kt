package com.nabto.webrtc

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ClientConnectivityTests {
    private lateinit var clientTestInstance: ClientTestInstance
    private lateinit var signalingClient: SignalingClient

    @Before
    fun setup() {
        clientTestInstance = createClientTestInstance()
        signalingClient = clientTestInstance.createSignalingClient();
    }

    @Test(timeout = 50000)
    fun client_connectivity_test1() = runBlocking {
        signalingClient.start();
        clientTestInstance.waitConnectionStates(listOf(SignalingConnectionState.CONNECTING, SignalingConnectionState.CONNECTED))
    }

    @Test()
    fun client_connectivity_test2() = runBlocking {
        signalingClient.start();
        clientTestInstance.waitConnectionStates(listOf(SignalingConnectionState.CONNECTING, SignalingConnectionState.CONNECTED))
        signalingClient.close();
        clientTestInstance.waitConnectionStates(listOf(SignalingConnectionState.CONNECTING, SignalingConnectionState.CONNECTED, SignalingConnectionState.CLOSED))
    }
}

@RunWith(AndroidJUnit4::class)
class ClientConnectivityTestsFailOptions {
    @Test(timeout = 5000)
    fun client_connectivity_test3() = runBlocking {
        val clientTestInstance =
            createClientTestInstance(ClientTestInstanceOptions(failHttp = true))
        val signalingClient = clientTestInstance.createSignalingClient();
        signalingClient.start();
        clientTestInstance.waitConnectionStates(
            listOf(
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.FAILED
            )
        );
        clientTestInstance.waitForError();
        assertEquals(1, clientTestInstance.observedErrors.size);
    }

    @Test(timeout = 5000)
    fun client_connectivity_test4() = runBlocking {
        val clientTestInstance = createClientTestInstance(ClientTestInstanceOptions(failWs = true))
        val signalingClient = clientTestInstance.createSignalingClient();
        signalingClient.start();
        clientTestInstance.waitConnectionStates(
            listOf(
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.FAILED
            )
        );
        clientTestInstance.waitForError();
        assertEquals(1, clientTestInstance.observedErrors.size);
    }

    @Test(timeout = 5000)
    fun client_connectivity_test5() = runBlocking {
        val clientTestInstance = createClientTestInstance()
        val signalingClient = clientTestInstance.createSignalingClient();
        signalingClient.start();
        clientTestInstance.waitConnectionStates(
            listOf(
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.CONNECTED
            )
        );
        clientTestInstance.closeWebsocket();
        clientTestInstance.waitConnectionStates(
            listOf(
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.CONNECTED,
                SignalingConnectionState.WAIT_RETRY,
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.CONNECTED
            ), 4000
        );
    }

    @Test(timeout = 5000)
    fun client_connectivity_test6() = runBlocking {
        val clientTestInstance =
            createClientTestInstance(ClientTestInstanceOptions(extraClientConnectResponseData = true))
        val signalingClient = clientTestInstance.createSignalingClient();
        signalingClient.start();
        clientTestInstance.waitConnectionStates(
            listOf(
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.CONNECTED
            )
        );
    }
    @Test(timeout = 60000)
    fun client_connectivity_test7() = runBlocking {
        val clientTestInstance =
            createClientTestInstance()
        val signalingClient = clientTestInstance.createSignalingClient();
        signalingClient.start();
        clientTestInstance.waitConnectionStates(
            listOf(
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.CONNECTED
            )
        );
        clientTestInstance.sendUnknwonWebsocketMessageType();
        clientTestInstance.connectDevice();
        val messages = listOf(TestObject());
        clientTestInstance.sendMessageToClient(messages)
        clientTestInstance.waitReceivedMessages(messages)
    }

    @Test(timeout = 60000)
    fun client_connectivity_test8() = runBlocking {
        val clientTestInstance =
            createClientTestInstance()
        val signalingClient = clientTestInstance.createSignalingClient();
        signalingClient.start();
        clientTestInstance.waitConnectionStates(
            listOf(
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.CONNECTED
            )
        );
        clientTestInstance.sendNewFieldInKnownMessageType();
        clientTestInstance.connectDevice();
        val messages = listOf(TestObject());
        clientTestInstance.sendMessageToClient(messages)
        clientTestInstance.waitReceivedMessages(messages)
    }
    @Test(timeout = 60000)
    fun client_connectivity_test9() = runBlocking {
        val clientTestInstance =
            createClientTestInstance()
        val signalingClient = clientTestInstance.createSignalingClient();
        signalingClient.start();
        clientTestInstance.waitConnectionStates(
            listOf(
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.CONNECTED
            )
        );
        clientTestInstance.dropClientMessages();
        signalingClient.checkAlive();
        clientTestInstance.waitConnectionStates(
            listOf(
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.CONNECTED,
                SignalingConnectionState.WAIT_RETRY,
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.CONNECTED,
            )
        );
        val activeWebSockets : Int = clientTestInstance.getActiveWebSockets().toInt();
        assertEquals(1, activeWebSockets);
    }

    @Test(timeout = 60000)
    fun client_connectivity_test10() = runBlocking {
        val clientTestInstance =
            createClientTestInstance()
        val signalingClient = clientTestInstance.createSignalingClient();
        signalingClient.start();
        clientTestInstance.connectDevice();
        val errorMessage = "Channel closed";
        clientTestInstance.deviceSendError(SignalingError.CHANNEL_CLOSED, errorMessage);
        clientTestInstance.waitForError();
        val error = clientTestInstance.observedErrors[0];
        assert(error is SignalingError);
        if (error is SignalingError) {
            assertEquals(error.errorCode, SignalingError.CHANNEL_CLOSED);
            assertEquals(error.errorMessage, errorMessage);
        }
    }
    @Test(timeout = 60000)
    fun client_connectivity_test11() = runBlocking {
        val clientTestInstance =
            createClientTestInstance()
        val signalingClient = clientTestInstance.createSignalingClient();
        signalingClient.start();
        clientTestInstance.waitConnectionStates(
            listOf(
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.CONNECTED
            )
        );
        clientTestInstance.connectDevice();
        signalingClient.close();
        val error = clientTestInstance.waitForDeviceToReceiveError(1000.0);
        assert(error != null);
        assertEquals(SignalingError.CHANNEL_CLOSED, error?.errorCode);
        assertEquals("The channel has been closed by the application.", error?.errorMessage);
    }
}