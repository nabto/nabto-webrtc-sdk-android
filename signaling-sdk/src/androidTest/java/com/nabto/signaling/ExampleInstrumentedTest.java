package com.nabto.signaling;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.nabto.signaling.impl.Backend;

import java.util.concurrent.ExecutionException;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.nabto.signaling.test", appContext.getPackageName());
    }

    @Test
    public void testGetIceServers() throws ExecutionException, InterruptedException {
        // @TODO: Improve this test
        Backend backend = new Backend("https://eu.webrtc.dev.nabto.net", "wp-wrvinm7e", "wd-kennic9i");
        var future = backend.getIceServers();
        var iceServers = future.get();
        assertEquals("test", iceServers);
    }

    @Test
    public void testCreateSignalingClient() {
        var opts = new SignalingClientFactory.Options()
                .setEndpointUrl("https://eu.webrtc.nabto.net")
                .setProductId("wp-apy9i4ab")
                .setDeviceId("wd-fxb4zxg7nyf7sf3w");

        try (var client = SignalingClientFactory.createSignalingClient(opts)) {
            var f = client.connect();
            f.get();
            MessageSigner signer = new SharedSecretMessageSigner("MySecret", "default");
            var signed = signer.signMessage(new SignalingMessage("CREATE_REQUEST").toJson());
            client.getSignalingChannel().sendMessage(signed);
            Thread.sleep(3000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSharedSecretSignMessage() {
        MessageSigner signer = new SharedSecretMessageSigner("MySecret", "default");
        var input = "Hello World";
        var signed = signer.signMessage(input);
        var decoded = signer.verifyMessage(signed);
        assertEquals(input, decoded);
    }
}