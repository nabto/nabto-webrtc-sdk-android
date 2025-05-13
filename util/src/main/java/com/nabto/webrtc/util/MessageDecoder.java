package com.nabto.webrtc.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MessageDecoder {
    public SignalingMessageUnion decodeMessage(String message) throws IOException {
        SignalingMessageUnion result = new SignalingMessageUnion();
        String type = "";
        try {
            JSONObject json = new JSONObject(message);
            type = json.getString("type");
        } catch (JSONException e) {
            throw new IOException(e);
        }

        switch (type) {
            case SignalingMessageType.CANDIDATE:
                result.setCandidate(JsonUtil.fromJson(SignalingCandidate.class, message));
                break;
            case SignalingMessageType.DESCRIPTION:
                result.setDescription(JsonUtil.fromJson(SignalingDescription.class, message));
                break;
            case SignalingMessageType.CREATE_REQUEST:
                result.setCreateRequest(JsonUtil.fromJson(SignalingCreateRequest.class, message));
                break;
            case SignalingMessageType.CREATE_RESPONSE:
                result.setCreateResponse(JsonUtil.fromJson(SignalingCreateResponse.class, message));
                break;
            default:
                throw new IOException("DefaultMessageEncoder::decodeMessage input argument is invalid! " + message + " is not a valid signaling message.");
        }

        return result;
    }
}
