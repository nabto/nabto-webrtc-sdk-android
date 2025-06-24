package com.nabto.webrtc.util.impl;

import org.json.JSONObject;

public interface MessageSigner {
    JSONObject signMessage(JSONObject message);
    JSONObject verifyMessage(JSONObject token);
}
