package com.nabto.signaling.impl;

import org.json.JSONException;
import org.json.JSONObject;

public class ReliabilityMessage {
    public enum MessageType {
        ACK,
        MESSAGE
    }

    public MessageType type;
    public int seq;
    public String message;

    public ReliabilityMessage(MessageType type, int seq, String message) {
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
}
