package com.nabto.webrtc.util;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.nabto.webrtc.SignalingClientFactory;

import java.util.Optional;

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
        assertEquals("com.nabto.webrtc.util.test", appContext.getPackageName());
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
            MessageSigner signer = new JWTMessageSigner("MySecret", Optional.of("default"));
            var signed = signer.signMessage(new SignalingSetupRequest().toJson());
            client.sendMessage(signed);
            Thread.sleep(3000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSharedSecretSignMessage() throws JSONException {
        MessageSigner signer = new JWTMessageSigner("MySecret", Optional.of("default"));
        var input = new JSONObject();
        input.put("foo", "bar");
        var signed = signer.signMessage(input);
        var decoded = signer.verifyMessage(signed);
        assertEquals(input, decoded);
    }
}