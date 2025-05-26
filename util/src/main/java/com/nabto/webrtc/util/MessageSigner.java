package com.nabto.webrtc.util;

import org.json.JSONObject;

public interface MessageSigner {
    JSONObject signMessage(JSONObject message);
    JSONObject verifyMessage(JSONObject token);
}
