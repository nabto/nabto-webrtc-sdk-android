package com.nabto.webrtc.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Reliability {
    public interface RoutingMessageSender {
        void sendRoutingMessage(ReliabilityMessage message);
    }

    private final Logger logger = Logger.getLogger("ReliabilityLayer");
    private final List<ReliabilityMessage> unackedMessages = new ArrayList<>();
    private int recvSeq = 0;
    private int sendSeq = 0;
    private RoutingMessageSender sender;

    public Reliability(RoutingMessageSender sender) {
        this.sender = sender;
    }

    /**
     * Send a reliable message
     * @param message The message to send
     */
    public void sendReliableMessage(String message) {
        var encoded = new ReliabilityMessage(
                ReliabilityMessage.MessageType.MESSAGE,
                sendSeq,
                message
        );
        sendSeq++;
        unackedMessages.add(encoded);
        sender.sendRoutingMessage(encoded);
    }

    /**
     *
     * @param message
     * @return
     */
    public String handleRoutingMessage(ReliabilityMessage message) {
        if (message.type == ReliabilityMessage.MessageType.ACK) {
            handleAck(message);
            return null;
        } else {
            return handleReliabilityMessage(message);
        }
    }

    private String handleReliabilityMessage(ReliabilityMessage message) {
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
        return message.message;
    }

    private void handleAck(ReliabilityMessage ack) {
        if (!unackedMessages.isEmpty()) {
            var firstItem = unackedMessages.get(0);
            if (firstItem.seq == ack.seq) {
                unackedMessages.remove(0);
            } else {
                logger.info("Received ACK with seq " + ack.seq + " but the current unacked message has seq " + firstItem.seq);
            }
        } else {
            logger.info("ACK was received, but there are no unacked messages.");
        }
    }

    private void sendUnackedMessages() {
        for (var msg : unackedMessages) {
            sender.sendRoutingMessage(msg);
        }
    }

    private void sendAck(int seq) {
        logger.info("Sending ACK with seq " + seq);
        var ack = new ReliabilityMessage(ReliabilityMessage.MessageType.ACK, seq, null);
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

    public boolean isInitialMessage(ReliabilityMessage message) {
        return message.type == ReliabilityMessage.MessageType.MESSAGE && message.seq == 0;
    }
}
