package com.nabto.webrtc.impl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ReliabilityData {
    public enum MessageType {
        ACK,
        DATA
    }

    public MessageType type;
    public int seq;

    public JSONObject data;

    public ReliabilityData(MessageType type, int seq, JSONObject data) {
        this.type = type;
        this.seq = seq;
        this.data = data;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            if (this.type == MessageType.DATA) {
                json.put("type", "DATA");
                json.put("data", data);
            } else {
                json.put("type", "ACK");
            }
            json.put("seq", seq);
        } catch (JSONException e) {
            throw new RuntimeException("Should never happen");
        }
        return json;
    }

    public static ReliabilityData fromJson(String data) throws JSONException {
        JSONObject json = new JSONObject(data);
        var type = json.get("type");
        if (Objects.equals(type, "ACK")) {
            var seq = json.getInt("seq");
            return new ReliabilityData(MessageType.ACK, seq, null);
        } else if (Objects.equals(type, "DATA")) {
            var seq = json.getInt("seq");
            var msg = json.getJSONObject("data");
            return new ReliabilityData(MessageType.DATA, seq, msg);
        } else {
            throw new JSONException("ReliabilityMessage type field was not ACK or DATA");
        }
    }
}
