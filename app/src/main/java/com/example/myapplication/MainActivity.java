package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nabto.webrtc.util.MessageSigner;
import com.nabto.webrtc.util.SharedSecretMessageSigner;
import com.nabto.webrtc.SignalingChannel;
import com.nabto.webrtc.SignalingChannelState;
import com.nabto.webrtc.SignalingClient;
import com.nabto.webrtc.SignalingClientFactory;
import com.nabto.webrtc.SignalingError;
import com.nabto.webrtc.util.SignalingCandidate;
import com.nabto.webrtc.util.SignalingCreateRequest;
import com.nabto.webrtc.util.SignalingDescription;
import com.nabto.webrtc.util.SignalingMessageUnion;

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

import io.getstream.webrtc.android.ui.VideoTextureViewRenderer;

public class MainActivity extends AppCompatActivity implements
        PeerConnection.Observer,
        SignalingChannel.Observer
{
    final String TAG = "MyApp";
    final String endpointUrl = "https://eu.webrtc.nabto.net";
    final String productId = "wp-apy9i4ab";
    final String deviceId = "wd-fxb4zxg7nyf7sf3w";
    final String sharedSecret = "MySecret";

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
    MessageSigner signer = new SharedSecretMessageSigner(sharedSecret, "default");
    SignalingClient client;

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
        client.connect().whenComplete((res, ex) -> {
            if (ex == null) {
                onClientConnected();
            }
        });
    }

    private void onClientConnected() {
        var channel = client.getSignalingChannel();
        channel.addObserver(this);
        var createRequestMessage = new SignalingCreateRequest();
        channel.sendMessage(signer.signMessage(createRequestMessage.toJson()));
    }

    private void setupPeerConnection(List<PeerConnection.IceServer> iceServers) {
        var rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
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
        var signalingDescription = new SignalingDescription(desc.type.canonicalForm(), desc.description).toJson();
        var signed = signer.signMessage(signalingDescription);
        client.getSignalingChannel().sendMessage(signed);
    }

    private void sendIceCandidate(IceCandidate candidate) {
        var signalingCandidate = new SignalingCandidate(candidate.sdp)
                .withSdpMid(candidate.sdpMid)
                .withSdpMLineIndex(candidate.sdpMLineIndex)
                .toJson();
        var signed = signer.signMessage(signalingCandidate);
        client.getSignalingChannel().sendMessage(signed);
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
    public void onMessage(String message) {
        try {
            // verify and decode message
            var verified = signer.verifyMessage(message);
            var msg = SignalingMessageUnion.fromJson(verified);

            if (msg.isDescription()) {
                setRemoteDescription(msg.getDescription().description);
            }

            if (msg.isCandidate()) {
                addIceCandidate(msg.getCandidate().candidate);
            }

            if (msg.isCreateRequest()) {
                throw new RuntimeException("Received createRequest but I'm a client?");
            }

            if (msg.isCreateResponse()) {
                var iceServers = new ArrayList<PeerConnection.IceServer>();
                var response = msg.getCreateResponse();
                for (var iceServer : response.iceServers) {
                    var builder = PeerConnection.IceServer.builder(iceServer.urls);
                    if (iceServer.username != null) { builder.setUsername(iceServer.username); }
                    if (iceServer.credential != null) { builder.setPassword(iceServer.credential); }
                    iceServers.add(builder.createIceServer());
                }

                setupPeerConnection(iceServers);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onChannelStateChange(SignalingChannelState newState) {
        Log.d(TAG, "Signaling channel state changed to " + newState.name());
    }

    @Override
    public void onSignalingReconnect() {
        Log.d(TAG, "Signaling reconnect requested");
    }

    @Override
    public void onSignalingError(SignalingError error) {
        Log.d(TAG, error.errorCode);
    }
}