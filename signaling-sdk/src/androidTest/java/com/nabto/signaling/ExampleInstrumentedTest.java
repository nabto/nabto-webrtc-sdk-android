package com.nabto.signaling;

import android.content.Context;

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
                .setEndpointUrl("https://eu.webrtc.dev.nabto.net")
                .setProductId("wp-wrvinm7e")
                .setDeviceId("wd-kennic9i");

        try (var client = SignalingClientFactory.createSignalingClient(opts)) {
            var f = client.connect();
            f.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}