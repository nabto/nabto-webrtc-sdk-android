package com.nabto.webrtc.util;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;

public class JsonUnitTest {
    @Test
    public void canParseSuccessfully() throws IOException {
        var json = "{\"urls\": [\"123.com\"]}";
        var iceServer = SignalingIceServer.fromJson(json);
        assertEquals(iceServer.urls.get(0), "123.com");
        assertNull(iceServer.credential);
        assertNull(iceServer.username);
    }

    @Test
    public void canBeExtended() throws IOException {
        var json = "{\"urls\": [\"456.com\"], \"someUnrelatedFieldUnlikelyToEverBeUsed\": 23, \"username\": \"foobar\"}";
        var iceServer = SignalingIceServer.fromJson(json);
        assertEquals(iceServer.urls.get(0), "456.com");
        assertEquals(iceServer.username, "foobar");
    }
}