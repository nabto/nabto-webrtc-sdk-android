package com.example.myapplication;


import android.os.Bundle;
import android.util.Log;

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
import com.nabto.webrtc.util.WebRTCSignalingMessageUnion;
import com.nabto.webrtc.util.impl.SignalingMessageUnion;

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
import java.util.Optional;
import java.util.stream.Collectors;

import io.getstream.webrtc.android.ui.VideoTextureViewRenderer;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MyApp";
    final String endpointUrl = "https://eu.webrtc.nabto.net";
    final String productId = BuildConfig.PRODUCT_ID;
    final String deviceId = BuildConfig.DEVICE_ID;
    final String sharedSecret = BuildConfig.SHARED_SECRET;

    // Webrtc
    PeerConnectionFactory peerConnectionFactory = null;
    PeerConnection peerConnection = null;
    PerfectNegotiation perfectNegotiation = null;
    WebRTCLogger logger = new WebRTCLogger();
    EglBase eglBase = EglBase.create();
    VideoTextureViewRenderer videoView;

    // Nabto signaling
    SignalingClient client;
    MessageTransport messageTransport;

    private void initPeerConnectionFactory() {
        var initOptions = PeerConnectionFactory
                .InitializationOptions
                .builder(this)
                .createInitializationOptions();
        PeerConnectionFactory.initialize(initOptions);
        org.webrtc.Logging.enableLogToDebugOutput(Logging.Severity.LS_VERBOSE);

        var encoderFactory = new DefaultVideoEncoderFactory(eglBase.getEglBaseContext(), true, true);
        var decoderFactory = new DefaultVideoDecoderFactory(eglBase.getEglBaseContext());

        peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();

        videoView = findViewById(R.id.videoView);
        videoView.init(eglBase.getEglBaseContext(), new RendererCommon.RendererEvents() {
            @Override
            public void onFirstFrameRendered() {}
            @Override
            public void onFrameResolutionChanged(int i, int i1, int i2) {}
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
        var opts = new SignalingClientFactory.Options()
                .setEndpointUrl(endpointUrl)
                .setProductId(productId)
                .setDeviceId(deviceId);

        Log.d(TAG, "Creating signaling client");
        client = SignalingClientFactory.createSignalingClient(opts);
        messageTransport = ClientMessageTransport.createSharedSecretMessageTransport(client, sharedSecret, Optional.empty());
        messageTransport.addObserver(new LoggingMessageTransportObserverAdapter() {
            @Override
            public void onWebRTCSignalingMessage(WebRTCSignalingMessageUnion message) {
                super.onWebRTCSignalingMessage(message);
                perfectNegotiation.onMessage(message);
            }

            @Override
            public void onSetupDone(List<SignalingIceServer> iceServers) {
                super.onSetupDone(iceServers);
                setupPeerConnection(iceServers);
            }
        });
        client.addObserver(new LoggingSignalingClientObserverAdapter());
        client.start();
    }

    private void setupPeerConnection(List<SignalingIceServer> iceServers) {
        var iceServers2 = convertSignalingIceServersToPeerConnectionIceServers(iceServers);
        var rtcConfig = new PeerConnection.RTCConfiguration(iceServers2);
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new LoggingPeerConnectionObserverAdapter() {
            @Override public void onRenegotiationNeeded() {
                super.onRenegotiationNeeded();
                perfectNegotiation.onNegotiationNeeded();
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
