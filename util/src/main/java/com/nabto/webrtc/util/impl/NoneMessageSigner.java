package com.nabto.webrtc.util.impl;

import com.nabto.webrtc.SignalingError;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;

public class NoneMessageSigner implements MessageSigner {

    public NoneMessageSigner() {
    }

    @Override
    public JSONObject signMessage(JSONObject message) {
        try {
            var obj = new JSONObject();
            obj.put("type", "NONE");
            obj.put("message", message);
            return obj;
        } catch (JSONException e) {
            // @TODO: Better description
            throw new RuntimeException("NONEMessageSigner failed to sign message");
        }
    }

    @Override
    public JSONObject verifyMessage(JSONObject token) {
        try {
            String type = token.get("type").toString();
            JSONObject message = token.getJSONObject("message");
            if (!type.equals("NONE")) {
                throw new SignalingError(SignalingError.VERIFICATION_ERROR, MessageFormat.format("Expected a signing message of type NONE but got: {0}", type));
            }
            return message;
        } catch (Exception e) {
            throw new RuntimeException("NONEMessageSigner failed to verify message");
        }
    }
}
