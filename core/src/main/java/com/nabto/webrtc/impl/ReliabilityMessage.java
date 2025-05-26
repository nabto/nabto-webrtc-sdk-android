package com.nabto.webrtc.impl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ReliabilityMessage {
    public enum MessageType {
        ACK,
        MESSAGE
    }

    public MessageType type;
    public int seq;

    // @TODO: message should now be a jsonobject
    // @TODO: change "message" to "data"
    public JSONObject message;

    public ReliabilityMessage(MessageType type, int seq, JSONObject message) {
        this.type = type;
        this.seq = seq;
        this.message = message;
    }

    public String toJsonString() {
        try {
            JSONObject json = new JSONObject();
            if (this.type == MessageType.MESSAGE) {
                json.put("type", "MESSAGE");
                json.put("message", message);
            } else {
                json.put("type", "ACK");
            }
            json.put("seq", seq);
            return json.toString();
        } catch (JSONException e) {
            throw new RuntimeException("Should never happen");
        }
    }

    public static ReliabilityMessage fromJson(String data) throws JSONException {
        JSONObject json = new JSONObject(data);
        var type = json.get("type");
        if (Objects.equals(type, "ACK")) {
            var seq = json.getInt("seq");
            return new ReliabilityMessage(MessageType.ACK, seq, null);
        } else if (Objects.equals(type, "MESSAGE")) {
            var seq = json.getInt("seq");
            var msg = json.getJSONObject("message");
            return new ReliabilityMessage(MessageType.MESSAGE, seq, msg);
        } else {
            throw new JSONException("ReliabilityMessage type field was not ACK or MESSAGE");
        }
    }
}
