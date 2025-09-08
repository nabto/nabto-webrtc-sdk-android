package com.example.myapplication;


import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nabto.webrtc.util.ClientMessageTransport;
import com.nabto.webrtc.SignalingClient;
import com.nabto.webrtc.SignalingClientFactory;
import com.nabto.webrtc.util.MessageTransport;
import com.nabto.webrtc.util.SignalingIceServer;
import com.nabto.webrtc.util.WebrtcSignalingMessage;
import com.nabto.webrtc.util.org.webrtc.PerfectNegotiation;

import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpTransceiver;
import org.webrtc.VideoTrack;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import io.getstream.webrtc.android.ui.VideoTextureViewRenderer;


public class MainActivity extends AppCompatActivity {
    final String TAG = "MyApp";
    final String productId = "wp-xcbsh4gw"; // "wp-ooraxfzr";
    final String deviceId = "wd-kau39afoqxv3pqrx"; // "wd-jd9dhzgcttbe7tqe";
    final String sharedSecret = "23542349849172470921340972134097447210934792347092147092134709214702194734";

    // Webrtc
    PeerConnectionFactory peerConnectionFactory = null;
    PeerConnection peerConnection = null;
    PerfectNegotiation perfectNegotiation = null;
    EglBase eglBase = EglBase.create();
    VideoTextureViewRenderer videoView;

    // Nabto signaling
    SignalingClient client;
    MessageTransport messageTransport;
    SignalingClientFactory.Options signalingClientOptions;

    private void initPeerConnectionFactory() {
        var initOptions = PeerConnectionFactory
                .InitializationOptions
                .builder(this)
                .createInitializationOptions();
        PeerConnectionFactory.initialize(initOptions);
        org.webrtc.Logging.enableLogToDebugOutput(Logging.Severity.LS_INFO);

        var encoderFactory = new DefaultVideoEncoderFactory(eglBase.getEglBaseContext(), true, true);
        var decoderFactory = new DefaultVideoDecoderFactory(eglBase.getEglBaseContext());

        peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();

        Log.d(TAG, "Supported video encoder codecs:");
        for (var codec : encoderFactory.getSupportedCodecs()) {
            Log.d(TAG, "  Encoder: " + codec.name );
        }

        Log.d(TAG, "Supported video decoder codecs:");
        for (var codec : decoderFactory.getSupportedCodecs()) {
            Log.d(TAG, "  Decoder: " + codec.name );
        }

        videoView = findViewById(R.id.videoView);
        videoView.init(eglBase.getEglBaseContext(), new RendererCommon.RendererEvents() {
            @Override
            public void onFirstFrameRendered() {}
            @Override
            public void onFrameResolutionChanged(int width, int height, int rotation) {
                // fix such that the video is not cropped at the edges.
                runOnUiThread(() -> {
                    float aspectRatio = (rotation == 0 || rotation == 180) ? (float) width / height : (float) height / width;
                    int newWidth = videoView.getWidth();
                    int newHeight = (int) (newWidth / aspectRatio);
                    ViewGroup.LayoutParams params = videoView.getLayoutParams();
                    params.height = newHeight;
                    videoView.setLayoutParams(params);
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init RTCPeerConnectionFactory
        initPeerConnectionFactory();

        // Connect to Nabto Signaling
        signalingClientOptions = new SignalingClientFactory.Options()
                .setProductId(productId)
                .setDeviceId(deviceId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Creating signaling client");
        client = SignalingClientFactory.createSignalingClient(signalingClientOptions);
        messageTransport = ClientMessageTransport.createSharedSecretMessageTransport(client, sharedSecret);
        messageTransport.addObserver(new LoggingMessageTransportObserverAdapter() {
            @Override
            public void onWebrtcSignalingMessage(WebrtcSignalingMessage message) {
                super.onWebrtcSignalingMessage(message);
                perfectNegotiation.onMessage(message);
            }

            @Override
            public void onSetupDone(List<SignalingIceServer> iceServers) {
                super.onSetupDone(iceServers);
                setupPeerConnection(iceServers);
            }
        });
        client.addObserver(new LoggingSignalingClientObserverAdapter() {
            @Override
            public void onConnectionReconnect() {
                super.onConnectionReconnect();
                if (peerConnection != null) {
                    peerConnection.restartIce();
                }
            }
        });
        client.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.resumeVideo();
    }
    @Override
    protected void onPause() {
        super.onPause();
        videoView.pauseVideo();
    }

    @Override
    protected void onStop() {
        try {
            super.onStop();
            perfectNegotiation = null;
            if (peerConnection != null) {
                peerConnection.close();
                peerConnection = null;
            }
            if (messageTransport != null) {
                messageTransport.close();
                messageTransport = null;
            }
            if (client != null) {
                client.close();
                client = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "unhandled exception in onStop", e);
        }
    }

    private void setupPeerConnection(List<SignalingIceServer> iceServers) {
        var iceServers2 = convertSignalingIceServersToPeerConnectionIceServers(iceServers);
        var rtcConfig = new PeerConnection.RTCConfiguration(iceServers2);
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new LoggingPeerConnectionObserverAdapter() {
            @Override public void onRenegotiationNeeded() {
                super.onRenegotiationNeeded();
                perfectNegotiation.onNegotiationNeeded();
            }
            @Override public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                super.onConnectionChange(newState);
                // This logic tries to establish the connection if it gets disconnected or fails.
                if (newState == PeerConnection.PeerConnectionState.DISCONNECTED) {
                    client.checkAlive();
                }
                if (newState == PeerConnection.PeerConnectionState.FAILED) {
                    peerConnection.restartIce();
                }
            }
            @Override public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                perfectNegotiation.onIceCandidate(iceCandidate);
            }
            @Override public void onTrack(RtpTransceiver transceiver) {
                super.onTrack(transceiver);
                handleOnTrack(transceiver);
            }
        });
        this.perfectNegotiation = new PerfectNegotiation(peerConnection, messageTransport);
    }

    public void handleOnTrack(RtpTransceiver transceiver) {
        Log.d(TAG, "onTrack");
        if (transceiver != null) {
            var receiver = transceiver.getReceiver();
            if (receiver != null) {
                var track = receiver.track();
                if (track != null && Objects.equals(track.kind(), "video")) {
                    var videoTrack = (VideoTrack)track;
                    videoTrack.addSink(videoView);
                }
            }
        }
    }

    private List<PeerConnection.IceServer> convertSignalingIceServersToPeerConnectionIceServers(List<SignalingIceServer> iceServers) {
        return iceServers.stream().map(iceServer -> {
            var builder = PeerConnection.IceServer.builder(iceServer.urls);
            if (iceServer.username != null) { builder.setUsername(iceServer.username); }
            if (iceServer.credential != null) { builder.setPassword(iceServer.credential); }
            return builder.createIceServer();
        }).collect(Collectors.toList());
    }
}
