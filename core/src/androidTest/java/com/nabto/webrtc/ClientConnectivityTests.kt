package com.nabto.webrtc

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
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
        signalingClient.connect();
        clientTestInstance.waitConnectionStates(listOf(SignalingConnectionState.CONNECTING, SignalingConnectionState.CONNECTED))
    }
}