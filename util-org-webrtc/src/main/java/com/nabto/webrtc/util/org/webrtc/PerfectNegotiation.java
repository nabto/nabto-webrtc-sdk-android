package com.nabto.webrtc.util.org.webrtc;

import android.util.Log;

import com.nabto.webrtc.util.MessageTransport;
import com.nabto.webrtc.util.SignalingCandidate;
import com.nabto.webrtc.util.SignalingDescription;
import com.nabto.webrtc.util.WebrtcSignalingMessageUnion;

import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.concurrent.CompletableFuture;

/**
 * Convert an webrtc peer connection SdpObserver to a completeable future.
 */
class FutureSdpObserver implements SdpObserver {

    CompletableFuture<Void> future = new CompletableFuture<>();

    FutureSdpObserver() {

    }

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
    }

    @Override
    public void onSetSuccess() {
        future.complete(null);
    }

    @Override
    public void onCreateFailure(String s) {
        future.completeExceptionally(new Error(s));
    }

    @Override
    public void onSetFailure(String s) {
        future.completeExceptionally(new Error(s));
    }
}

/**
 * This class implements the perfect negotiation pattern from <a href="https://developer.mozilla.org/en-US/docs/Web/API/WebRTC_API/Perfect_negotiation">Perfect Negotiation</a>
 */
public class PerfectNegotiation {
    final String TAG = "PerfectNegotiation";
    boolean makingOffer = false;
    boolean ignoreOffer = false;
    boolean isSettingRemoteAnswerPending = false;
    PeerConnection pc;
    MessageTransport messageTransport;
    boolean polite;

    public PerfectNegotiation(PeerConnection peerConnection, MessageTransport messageTransport) {
        // devices acts as polite and clients are impolite.
        this.polite = (messageTransport.getMode() == MessageTransport.Mode.DEVICE);
        this.pc = peerConnection;
        this.messageTransport = messageTransport;
    }

    public void onMessage(WebrtcSignalingMessageUnion msg) {
        SignalingDescription description = msg.getDescription();
        SignalingCandidate candidate = msg.getCandidate();

        if (description != null) {
            var type = SessionDescription.Type.fromCanonicalForm(description.description.type);
            var desc = new SessionDescription(type, description.description.sdp);
            handleDescription(desc).exceptionally(ex -> {
                Log.e(TAG, "error handling description", ex);
                return null;
            });
        } else if (candidate != null) {
            var cand = new IceCandidate(candidate.candidate.sdpMid, candidate.candidate.getSdpMLineIndex(), candidate.candidate.candidate);
            handleCandidate(cand);
        }
    }

    public void onNegotiationNeeded() {
        makingOffer = true;

        setLocalDescription()
                .thenRun(() -> {
                    SessionDescription localDescription = pc.getLocalDescription();
                    sendDescription(localDescription);
                })
                .exceptionally(err -> {
                    Log.e(TAG, "onNegotiationNeeded failed", err);
                    return null;
                })
                .whenComplete((result, ex) -> makingOffer = false);
    }

    public void onIceCandidate(IceCandidate iceCandidate) {
        if (iceCandidate != null) {
            SignalingCandidate signalingCandidate = new SignalingCandidate(iceCandidate.sdp).withSdpMid(iceCandidate.sdpMid).withSdpMLineIndex(iceCandidate.sdpMLineIndex);
            this.messageTransport.sendWebrtcSignalingMessage(signalingCandidate);
        }
    }

    private CompletableFuture<Void> handleDescription(SessionDescription description) {


        synchronized (this) {
            boolean readyForOffer = !makingOffer &&
                    (pc.signalingState() == PeerConnection.SignalingState.STABLE || isSettingRemoteAnswerPending);

            boolean offerCollision = description.type == SessionDescription.Type.OFFER && !readyForOffer;
            ignoreOffer = !polite && offerCollision;

            if (ignoreOffer) {
                return CompletableFuture.completedFuture(null);
            }

            if (description.type == SessionDescription.Type.ANSWER) {
                isSettingRemoteAnswerPending = true;
            }
        }

        return setRemoteDescription(description)
                .thenCompose(v -> {
                    isSettingRemoteAnswerPending = false;
                    if (description.type == SessionDescription.Type.OFFER) {
                        return setLocalDescription()
                                .thenRun(() -> {
                                    SessionDescription localDesc = pc.getLocalDescription();
                                    sendDescription(localDesc);
                                });
                    } else {
                        return CompletableFuture.completedFuture(null);
                    }
                });
    }

    private void handleCandidate(IceCandidate candidate) {
        try {
            pc.addIceCandidate(candidate);
        } catch (Exception e) {
            if (!ignoreOffer) {
                return;
            }
            Log.e(TAG, "handleCandidate", e);
        }
    }

    void sendDescription(SessionDescription description) {
        SignalingDescription signalingDescription = new SignalingDescription(description.type.canonicalForm(), description.description);
        this.messageTransport.sendWebrtcSignalingMessage(signalingDescription);
    }

    CompletableFuture<Void> setRemoteDescription(SessionDescription sdp) {
        FutureSdpObserver futureSdpObserver = new FutureSdpObserver();
        this.pc.setRemoteDescription(futureSdpObserver, sdp);
        return futureSdpObserver.future;
    }

    CompletableFuture<Void> setLocalDescription() {
        FutureSdpObserver futureSdpObserver = new FutureSdpObserver();
        this.pc.setLocalDescription(futureSdpObserver);
        return futureSdpObserver.future;
    }

}
