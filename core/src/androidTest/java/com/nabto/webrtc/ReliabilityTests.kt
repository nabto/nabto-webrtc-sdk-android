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
class ReliabilityTests {
    @Test(timeout = 50000)
    fun reliability_test1() = runBlocking {
        val clientTestInstance =
            createClientTestInstance(ClientTestInstanceOptions())
        val signalingClient = clientTestInstance.createSignalingClient();
        signalingClient.start();
        clientTestInstance.waitConnectionStates(
            listOf(
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.CONNECTED
            )
        );
        clientTestInstance.connectDevice();
        clientTestInstance.sendMessageToClient(listOf(TestObject()))
        val recvObjects : List<TestObject> = listOf(TestObject())
        clientTestInstance.waitReceivedMessages(recvObjects, 1000);
    }
}