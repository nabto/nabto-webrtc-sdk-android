package com.nabto.webrtc;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import com.nabto.webrtc.impl.Backend;
import java.util.concurrent.ExecutionException;
import org.openapitools.client.apis.DefaultApi;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void testGetIceServers() throws ExecutionException, InterruptedException {
        // @TODO: Improve this test
        Backend backend = new Backend("https://eu.webrtc.dev.nabto.net", "wp-wrvinm7e", "wd-kennic9i");
        var future = backend.getIceServers();
        var iceServers = future.get();
        //assertEquals("test", iceServers);

    }
}
