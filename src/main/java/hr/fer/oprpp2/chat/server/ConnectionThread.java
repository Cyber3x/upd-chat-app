package hr.fer.oprpp2.chat.server;

import hr.fer.oprpp2.chat.client.ChatClient;
import hr.fer.oprpp2.chat.common.SentMessageMetadata;
import hr.fer.oprpp2.chat.common.messages.Ack;
import hr.fer.oprpp2.chat.common.messages.InMsg;
import hr.fer.oprpp2.chat.common.messages.Message;
import hr.fer.oprpp2.chat.common.messages.OutMsg;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionThread extends Thread {
    private final static int SO_TIMEOUT_MS = 2000;
    private final static int PACKET_RESEND_COUNT = 5;

    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private long messageNumber = 1;

    private final LinkedBlockingQueue<Message> incomingMessages = new LinkedBlockingQueue<>();

    private final ScheduledExecutorService scheduler;
    private final HashMap<Long, SentMessageMetadata> sentMessagesWaitingForAck = new HashMap<>();
    private final DatagramSocket serverSocket;
    private final SocketAddress clientSocketAddress;
    private final String clientSenderName;

    public ConnectionThread(DatagramSocket serverSocket, SocketAddress clientSocketAddress, String clientSenderName, ScheduledExecutorService scheduler) {
        this.serverSocket = serverSocket;
        this.clientSocketAddress = clientSocketAddress;
        this.clientSenderName = clientSenderName;
        this.scheduler = scheduler;

    }

    public void markForStopping() {
        isRunning.set(false);
    }

    @Override
    public void run() {
        System.out.println("Created thread for senderName = " + clientSenderName);
        Message currentMessage;

        while (isRunning.get()) {
            try {
                currentMessage = incomingMessages.take();
            } catch (InterruptedException e) {
                continue;
            }

            switch (currentMessage.getMessageTypeId()) {
                case OutMsg.MESSAGE_TYPE_ID -> {
                    OutMsg incomingMessage = (OutMsg) currentMessage;

                    InMsg outgoingMessage = new InMsg(messageNumber++, clientSenderName, incomingMessage.getMessageText());
                    sendMessage(outgoingMessage);
                }
                case Ack.MESSAGE_TYPE_ID -> {
                    Ack incommingAck = (Ack) currentMessage;
                    sentMessagesWaitingForAck.remove(incommingAck.getMessageNumber());
                }
                default -> System.err.println("Connection thread got unsupported packager.");
            }
        }
        System.out.println("Thread stopping, was for senderName = " + clientSenderName);
    }

    public void sendMessage(Message message) {
        try {
            long messageNumber = message.getMessageNumber();

            // Create a timer that will fire after socket timeout to check if we can resend this message
            scheduler.schedule(new RetransmitAction(message), SO_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            // check if this message is already waiting for an ACK
            SentMessageMetadata sentMessageMetadata = sentMessagesWaitingForAck.get(messageNumber);

            int messageResendsLeft = sentMessageMetadata == null ? PACKET_RESEND_COUNT : sentMessageMetadata.numberOfRetransmissionLeft() - 1;

            if (messageResendsLeft < 0) {
                System.out.println("No retransmissions left for messageNumber = " + messageNumber);
                sentMessagesWaitingForAck.remove(messageNumber);
                return;
            }

            // save the new metadata
            SentMessageMetadata newSentMessageMetadata = new SentMessageMetadata(message, messageResendsLeft);

            sentMessagesWaitingForAck.put(messageNumber, newSentMessageMetadata);
            System.out.println("Packet sent, metadata put into hashMap, messageNumber = " + messageNumber);

            // Send the message
            serverSocket.send(message.toPacket((InetSocketAddress) clientSocketAddress));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class RetransmitAction implements Runnable {
        private final Message message;

        public RetransmitAction(Message message) {
            this.message = message;
        }

        @Override
        public void run() {
            // check if the message is still in the waiting hash map
            SentMessageMetadata sentMessageMetadata = sentMessagesWaitingForAck.get(message.getMessageNumber());

            // the message is not in the hash map -> it received an ack and was removed
            if (sentMessageMetadata == null) return;

            System.out.printf("Retransmitting message with messageNumber = %d, retransmission left = %d\n", sentMessageMetadata.message().getMessageNumber(), sentMessageMetadata.numberOfRetransmissionLeft());
            sendMessage(message);
        }
    }

    public void addMessage(Message message) {
        incomingMessages.add(message);
    }
}
