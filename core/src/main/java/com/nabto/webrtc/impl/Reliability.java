package com.nabto.webrtc.impl;

import org.json.JSONObject;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class Reliability {
    public interface RoutingMessageSender {
        void sendRoutingMessage(ReliabilityData message);
    }

    private final Logger logger = Logger.getLogger("ReliabilityLayer");
    private final ConcurrentLinkedQueue<ReliabilityData> unackedMessages = new ConcurrentLinkedQueue<>();
    private int recvSeq = 0;
    private int sendSeq = 0;
    final private RoutingMessageSender sender;

    public Reliability(RoutingMessageSender sender) {
        this.sender = sender;
    }

    /**
     * Send a reliable message
     *
     * @param data The message to send
     */
    public void sendReliableMessage(JSONObject data) {
        ReliabilityData encoded;
        synchronized (this) {
            encoded = new ReliabilityData(
                    ReliabilityData.MessageType.DATA,
                    sendSeq,
                    data
            );

            sendSeq++;
            unackedMessages.add(encoded);
        }

        sender.sendRoutingMessage(encoded);
    }

    /**
     * This function is triggered from the websocket onmessage. That function is called from the
     * TCP recvthread so only one routing message will be handled at a time.
     *
     * @param message the routing message from the remote peer.
     * @return a JSONObject or null if the operation did not result in a new signaling message.
     */
    public JSONObject handleRoutingMessage(ReliabilityData message) {
        if (message.type == ReliabilityData.MessageType.ACK) {
            handleAck(message);
            return null;
        } else {
            return handleReliabilityMessage(message);
        }
    }

    private JSONObject handleReliabilityMessage(ReliabilityData message) {
        if (message.seq <= recvSeq) {
            // Message was expected or retransmitted.
            sendAck(message.seq);
        }

        if (message.seq != recvSeq) {
            // Message is out of order (from the past or from the future)
            logger.warning("Received a message with seq " + message.seq + ", when " + recvSeq + " was expected");
            return null;
        }

        recvSeq++;
        return message.data;
    }

    private synchronized void handleAck(ReliabilityData ack) {
        var firstItem = unackedMessages.peek();
        if (firstItem != null) {
            if (firstItem.seq == ack.seq) {
                unackedMessages.poll();
            } else {
                logger.info("Received ACK with seq " + ack.seq + " but the current unacked message has seq " + firstItem.seq);
            }
        } else {
            logger.info("ACK was received, but there are no unacked messages.");
        }
    }

    /**
     * In the unlike case where sendUnacked messages are called both from handlePeerConnected and
     * handleConnect at the same time the messages will still be sent in sequence but with possible
     * duplicates. The duplicates will be removed at the receiving client. We do not take a lock
     * here since we are unsure if the sendRoutingMessage can end in a state where it blocks while
     * it possible waits on available TCP send buffer space.
     */
    private void sendUnackedMessages() {
        for (var msg : unackedMessages) {
            sender.sendRoutingMessage(msg);
        }
    }

    private void sendAck(int seq) {
        logger.info("Sending ACK with seq " + seq);
        var ack = new ReliabilityData(ReliabilityData.MessageType.ACK, seq, null);
        sender.sendRoutingMessage(ack);
    }

    /**
     * Called if the remote peer has connected or reconnected.
     */
    public void handlePeerConnected() {
        sendUnackedMessages();
    }

    /**
     * Called if the underlying websocket connection has connected/reconnected.
     */
    public void handleConnect() {
        sendUnackedMessages();
    }
}
