package com.nabto.webrtc

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

    val clientTestInstance = createClientTestInstance(ClientTestInstanceOptions())
    val signalingClient = clientTestInstance.createSignalingClient();

    val testObject = TestObject();
    var testObjectEncodedAsJSONObject: JSONObject

    @OptIn(ExperimentalStdlibApi::class)
    constructor() {
        val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<TestObject> = moshi.adapter<TestObject>();
        val json = jsonAdapter.toJson(testObject);
        this.testObjectEncodedAsJSONObject = JSONObject(json);
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test(timeout = 50000)
    fun reliability_test1() = runBlocking {
        signalingClient.start();
        clientTestInstance.waitConnectionStates(
            listOf(
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.CONNECTED
            )
        );
        clientTestInstance.connectDevice();
        signalingClient.sendMessage(testObjectEncodedAsJSONObject);
        clientTestInstance.waitForDeviceToReceiveMessages(listOf(testObject), 1000.0);
    }

    @Test(timeout = 50000)
    fun reliability_test2() = runBlocking {
        signalingClient.start();
        clientTestInstance.waitConnectionStates(
            listOf(
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.CONNECTED
            )
        );
        clientTestInstance.connectDevice();
        clientTestInstance.sendMessageToClient(listOf(TestObject()))
        val recvObjects: List<TestObject> = listOf(TestObject())
        clientTestInstance.waitReceivedMessages(recvObjects, 1000);
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test(timeout = 50000)
    fun reliability_test3() = runBlocking {
        signalingClient.start();
        clientTestInstance.waitConnectionStates(
            listOf(
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.CONNECTED
            )
        );
        signalingClient.sendMessage(testObjectEncodedAsJSONObject);
        clientTestInstance.connectDevice();
        clientTestInstance.waitForDeviceToReceiveMessages(listOf(testObject), 1000.0);
    }

    @Test(timeout = 50000)
    fun reliability_test4() = runBlocking {
        signalingClient.start();
        clientTestInstance.connectDevice();
        clientTestInstance.waitConnectionStates(
            listOf(
                SignalingConnectionState.CONNECTING,
                SignalingConnectionState.CONNECTED
            )
        );
        signalingClient.sendMessage(testObjectEncodedAsJSONObject);
        clientTestInstance.waitForDeviceToReceiveMessages(listOf(testObject), 1000.0);
        clientTestInstance.dropClientMessages();
        signalingClient.sendMessage(testObjectEncodedAsJSONObject);
        signalingClient.checkAlive();
        clientTestInstance.waitForDeviceToReceiveMessages(listOf(testObject, testObject), 5000.0);
    }

    @Test(timeout = 50000)
    fun reliability_test5() = runBlocking {
        signalingClient.start();
        clientTestInstance.connectDevice();
        clientTestInstance.dropClientMessages();
        clientTestInstance.sendMessageToClient(listOf(testObject));
        clientTestInstance.waitReceivedMessages(listOf(testObject));
        signalingClient.checkAlive();
        val testObject2 = TestObject(foo = "2");
        clientTestInstance.sendMessageToClient(listOf(testObject2));
        clientTestInstance.waitReceivedMessages(listOf(testObject, testObject2));
    }
    @Test(timeout = 50000)
    fun reliability_test6() = runBlocking {
        signalingClient.start();
        clientTestInstance.connectDevice();
        clientTestInstance.dropDeviceMessages();
        signalingClient.sendMessage(testObjectEncodedAsJSONObject);
        // the device is not receiving the message.
        // reconnect the device
        clientTestInstance.disconnectDevice();
        clientTestInstance.connectDevice();
        clientTestInstance.waitForDeviceToReceiveMessages(listOf(testObject), 1000.0);
    }
}

