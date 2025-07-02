package com.nabto.webrtc.util;

import com.nabto.webrtc.util.impl.JsonUtil;
import com.nabto.webrtc.util.impl.SignalingMessage;
import com.nabto.webrtc.util.impl.SignalingMessageType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Class representing a WebRTC Description received or to be sent on the
 * MessageTransport.
 */
public class SignalingDescription implements SignalingMessage {
    public final String type = SignalingMessageType.DESCRIPTION;

    /**
     * Field containing the information in the description.
     */
    public final Description description = new Description();

    /**
     * Description information class definition
     */
    public static class Description {
        /**
         * The description type (typically "offer" or "answer")
         */
        public String type;

        /**
         * SDP of the description.
         */
        public String sdp;
    }

    /**
     * Construct a SignalingDescription object to send
     *
     * @param type type of the description, typically "offer" or "answer"
     * @param sdp SDP representation of the description.
     */
    public SignalingDescription(String type, String sdp) {
        this.description.type = type;
        this.description.sdp = sdp;
    }

    /**
     * Convert the description to JSON.
     *
     * @return The resulting JSON object.
     */
    @Override
    public JSONObject toJson() {
        try {
            return new JSONObject(toJsonString());
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    /**
     * Convert the description to stringified JSON.
     *
     * @return The resulting JSON string.
     */
    @Override
    public String toJsonString() {
        return JsonUtil.toJson(SignalingDescription.class, this);
    }

    /**
     * Create a description from a JSON string.
     *
     * @param json The JSON string to parse
     * @return The resulting description object.
     */
    public static SignalingDescription fromJson(String json) throws IOException {
        return JsonUtil.fromJson(SignalingDescription.class, json);
    }
}
