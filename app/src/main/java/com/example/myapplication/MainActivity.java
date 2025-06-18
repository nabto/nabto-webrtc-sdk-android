package com.example.myapplication;


import com.example.myapplication.BuildConfig;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nabto.webrtc.SignalingConnectionState;
import com.nabto.webrtc.util.ClientMessageTransport;
import com.nabto.webrtc.util.JWTMessageSigner;
import com.nabto.webrtc.util.MessageSigner;
import com.nabto.webrtc.SignalingChannelState;
import com.nabto.webrtc.SignalingClient;
import com.nabto.webrtc.SignalingClientFactory;
import com.nabto.webrtc.SignalingError;
import com.nabto.webrtc.util.MessageTransport;
import com.nabto.webrtc.util.SignalingCandidate;
import com.nabto.webrtc.util.SignalingDescription;
import com.nabto.webrtc.util.SignalingIceServer;
import com.nabto.webrtc.util.SignalingMessageUnion;
import com.nabto.webrtc.util.SignalingSetupRequest;

import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import io.getstream.webrtc.android.ui.VideoTextureViewRenderer;

public class MainActivity extends AppCompatActivity implements
        PeerConnection.Observer,
        SignalingClient.Observer,
        MessageTransport.Observer
{
    final String TAG = "MyApp";
    final String endpointUrl = "https://eu.webrtc.nabto.net";
    final String productId = BuildConfig.PRODUCT_ID;
    final String deviceId = BuildConfig.DEVICE_ID;
    final String sharedSecret = BuildConfig.SHARED_SECRET;

    // Webrtc
    PeerConnectionFactory peerConnectionFactory = null;
    PeerConnection peerConnection = null;
    WebRTCLogger logger = new WebRTCLogger();
    EglBase eglBase = EglBase.create();
    VideoTextureViewRenderer videoView;
    boolean polite = false;
    boolean makingOffer = false;
    boolean ignoreOffer = false;

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
        messageTransport.addObserver(this);
        client.addObserver(this);
        client.connect().whenComplete((res, ex) -> {
            if (ex == null) {
                onClientConnected();
            }
        });
    }

    private void onClientConnected() {
    }

    private void setupPeerConnection(List<SignalingIceServer> iceServers) {
        var iceServers2 = iceServers.stream().map(iceServer -> {
            var builder = PeerConnection.IceServer.builder(iceServer.urls);
            if (iceServer.username != null) { builder.setUsername(iceServer.username); }
            if (iceServer.credential != null) { builder.setPassword(iceServer.credential); }
            return builder.createIceServer();
        }).collect(Collectors.toList());
        var rtcConfig = new PeerConnection.RTCConfiguration(iceServers2);
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, this);
    }

    private void addIceCandidate(SignalingCandidate.Candidate candidate) {
        try {
            var cand = new IceCandidate(candidate.sdpMid, 0, candidate.candidate);
            peerConnection.addIceCandidate(cand);
        } catch (Exception e) {
            if (!this.ignoreOffer) {
                Log.d(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    private void setRemoteDescription(SignalingDescription.Description description) {
        try {
            var collision = Objects.equals(description.type, "offer") && (makingOffer || peerConnection.signalingState() != PeerConnection.SignalingState.STABLE);

            ignoreOffer = !polite && collision;
            if (ignoreOffer) {
                return;
            }

            var type = SessionDescription.Type.fromCanonicalForm(description.type);
            var desc = new SessionDescription(SessionDescription.Type.OFFER, description.sdp);
            peerConnection.setRemoteDescription(new SdpObserver() {
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {}
                @Override
                public void onSetSuccess() {
                    if (type == SessionDescription.Type.OFFER) {
                        peerConnection.setLocalDescription(new SdpObserver() {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {}
                            @Override
                            public void onSetSuccess() {
                                sendDescription(peerConnection.getLocalDescription());
                            }
                            @Override
                            public void onCreateFailure(String s) {}
                            @Override
                            public void onSetFailure(String s) {}
                        });
                    }
                }
                @Override
                public void onCreateFailure(String s) {}
                @Override
                public void onSetFailure(String s) {}
            }, desc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendDescription(SessionDescription desc) {
        messageTransport.sendWebRTCSignalingMessage(new SignalingDescription(desc.type.canonicalForm(), desc.description));
    }

    private void sendIceCandidate(IceCandidate candidate) {
        messageTransport.sendWebRTCSignalingMessage(new SignalingCandidate(candidate.sdp)
                .withSdpMid(candidate.sdpMid)
                .withSdpMLineIndex(candidate.sdpMLineIndex));
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d(TAG, "onSignalingChange ==> " + signalingState.name());
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(TAG, "onIceConnectionChange ==> " + iceConnectionState.name());
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.d(TAG, "onIceConnectionReceivingChange");
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.d(TAG, "onIceGatheringChange ==> " + iceGatheringState.name());
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        if (iceCandidate != null) {
            sendIceCandidate(iceCandidate);
        }
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        Log.d(TAG, "onIceCandidatesRemoved");
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(TAG, "onAddStream");
    }

    @Override
    public void onTrack(RtpTransceiver transceiver) {
        PeerConnection.Observer.super.onTrack(transceiver);
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

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(TAG, "onRemoveStream");
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.d(TAG, "onDataChannel");
    }

    @Override
    public void onRenegotiationNeeded() {
        makingOffer = true;
        peerConnection.setLocalDescription(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {

            }

            @Override
            public void onSetSuccess() {
                sendDescription(peerConnection.getLocalDescription());
                makingOffer = false;
            }

            @Override
            public void onCreateFailure(String s) {
                makingOffer = false;
            }

            @Override
            public void onSetFailure(String s) {
                makingOffer = false;
            }
        });
    }

    @Override
    public void onConnectionStateChange(SignalingConnectionState newState) {

    }

    @Override
    public void onMessage(JSONObject message) {

    }

    @Override
    public void onChannelStateChange(SignalingChannelState newState) {
        Log.d(TAG, "Signaling channel state changed to " + newState.name());
    }

    @Override
    public void onConnectionReconnect() {
        Log.d(TAG, "Signaling reconnect requested");
    }

    @Override
    public void onError(SignalingError error) {
        Log.d(TAG, error.errorCode);
    }

    @Override
    public void onWebRTCSignalingMessage(SignalingMessageUnion message) {
        if (message.isDescription()) {
            setRemoteDescription(message.getDescription().description);
        }

        if (message.isCandidate()) {
            addIceCandidate(message.getCandidate().candidate);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.d(TAG, error.toString());
    }

    @Override
    public void onSetupDone(List<SignalingIceServer> iceServers) {
        setupPeerConnection(iceServers);
    }
}