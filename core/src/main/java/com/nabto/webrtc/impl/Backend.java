package com.nabto.webrtc.impl;

import androidx.annotation.NonNull;

import com.nabto.webrtc.DeviceIdNotFoundException;
import com.nabto.webrtc.HttpException;
import com.nabto.webrtc.ProductIdNotFoundException;
import com.squareup.moshi.Moshi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Backend {
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final Moshi moshi = new Moshi.Builder().build();
    private final String baseUrl;
    private final String productId;
    private final String deviceId;

    public static class ResponseException extends IOException {
        private int errorCode;

        public ResponseException(int errorCode, String message) {
            super("Error code :: " + message);
            this.errorCode = errorCode;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }

    private static class BackendErrorResponse {
        public String message;
        public String code;
    }

    public static class ClientConnectResponse {
        public String signalingUrl;
        public boolean deviceOnline;
        public String channelId;
        public String reconnectToken;
    }

    public Backend(String baseUrl, String productId, String deviceId) {
        this.baseUrl = baseUrl;
        this.productId = productId;
        this.deviceId = deviceId;
    }

    public CompletableFuture<String> getIceServers() {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("productId", productId);
            jsonBody.put("deviceId", deviceId);
        } catch (JSONException ignored) {}


        Request request = new Request.Builder()
                .url(baseUrl + "/v1/ice-servers")
                .post(RequestBody.create(jsonBody.toString(), MEDIA_TYPE_JSON))
                .build();

        CompletableFuture<String> future = new CompletableFuture<>();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try (ResponseBody responseBody = response.body()) {
                        future.complete(responseBody.string());
                    }
                } else {
                    Throwable e = handleError(response);
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }

    private Throwable handleError(@NonNull Response response) {
        try {
            try (ResponseBody responseBody = response.body()) {
                var adapter = moshi.adapter(BackendErrorResponse.class);
                var respString = responseBody.string();
                BackendErrorResponse errorResponse = adapter.fromJson(respString);
                String errorCode = errorResponse.code;
                if (errorCode != null) {
                    if (errorCode.equals("PRODUCT_ID_NOT_FOUND")) {
                        return new ProductIdNotFoundException(response.code(), errorResponse.message);
                    } else if (errorCode.equals("DEVICE_ID_NOT_FOUND")) {
                        return new DeviceIdNotFoundException(response.code(), errorResponse.message);
                    }
                }
                // fall through
                return new HttpException(response.code(), errorResponse.message);
            }
        } catch (Exception e) {
            return new HttpException(response.code(), response.message());
        }
    }

    public CompletableFuture<ClientConnectResponse> doClientConnect(String authToken) {
        var future = new CompletableFuture<ClientConnectResponse>();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("productId", productId);
            jsonBody.put("deviceId", deviceId);
        } catch (JSONException ignored) {}

        var builder = new Request.Builder()
                .url(baseUrl + "/v1/client/connect")
                .post(RequestBody.create(jsonBody.toString(), MEDIA_TYPE_JSON));

        if (authToken != null && !authToken.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }

        var req = builder.build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try (ResponseBody responseBody = response.body()) {
                        try {
                            var adapter = moshi.adapter(ClientConnectResponse.class);
                            var respString = responseBody.string();
                            ClientConnectResponse clientInfo = adapter.fromJson(respString);
                            future.complete(clientInfo);
                        } catch (Exception e) {
                            future.completeExceptionally(e);
                            return;
                        }
                    }
                } else {
                    Throwable e = handleError(response);
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }
}
