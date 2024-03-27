package hr.fer.oprpp2.chat.client;

import hr.fer.oprpp2.chat.common.SentMessageMetadata;
import hr.fer.oprpp2.chat.common.messages.*;

import java.io.IOException;
import java.net.*;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ChatClient implements IChatClient {
    private static final int SO_TIMEOUT_MS = 2000;
    private static final int PACKET_RESEND_COUNT = 3;

    private final String senderName;
    private final InetSocketAddress serverSocketAddress;
    private final DatagramSocket clientSocket;
    private final MessagesListModel messagesListModel;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // This map stores packet's that need to wait for their ack packet, format - PacketNumber: Pair(DatagramPacket, RetriesLeft)
    private final ConcurrentHashMap<Long, SentMessageMetadata> sentMessagesWaitingForAck = new ConcurrentHashMap<>();

    // this clients UID (User ID)
    private long UID;

    // Keep track of the packet numbering
    private long currentMessageNumber = 0;


    public ChatClient(String hostname, int port, String senderName, MessagesListModel messagesListModel) {
        this.senderName = senderName;
        this.serverSocketAddress = createServerSocketAddress(hostname, port);
        this.clientSocket = createClientSocket();
        this.messagesListModel = messagesListModel;

        ReceiverThread receiverThread = new ReceiverThread(this, clientSocket, sentMessagesWaitingForAck);
        receiverThread.start();

        sendHelloMessage();
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
                System.out.println("No retransmissions left for message: " + messageNumber);
                sentMessagesWaitingForAck.remove(messageNumber);
                return;
            }

            // save the new metadata
            SentMessageMetadata newSentMessageMetadata = new SentMessageMetadata(message, messageResendsLeft);

            sentMessagesWaitingForAck.put(messageNumber, newSentMessageMetadata);
            System.out.println("metadata put into hashMap for message: " + messageNumber);

            // Send the message
            clientSocket.send(message.toPacket(serverSocketAddress));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendAck(Ack ack) {
        try {
            clientSocket.send(ack.toPacket(serverSocketAddress));
            System.out.println("sent ack for message number: " + ack.getMessageNumber());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendTextMessage(String textMessage) {
        sendMessage(new OutMsg(currentMessageNumber++, UID, textMessage));
    }

    @Override
    public void handleIncomingMessage(InMsg message) {
        Ack ack = new Ack(message.getMessageNumber(), UID);
        sendAck(ack);
        messagesListModel.addMessage(serverSocketAddress.getHostName(), serverSocketAddress.getPort(), message);
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

            System.out.printf("retransmitting message with number: %d, retransmission left: %d\n", sentMessageMetadata.message().getMessageNumber(), sentMessageMetadata.numberOfRetransmissionLeft());
            sendMessage(message);
        }
    }

    private void sendHelloMessage() {
        System.out.println("sent hello msg");
        Message helloMessage = new Hello(currentMessageNumber++, senderName, new Random().nextLong());

        sendMessage(helloMessage);
    }


    public void sendByeMessage() {
        System.out.println("sent by msg");
        Message byeMessage = new Bye(currentMessageNumber++, UID);

        sendMessage(byeMessage);
    }

    public void initiateDisconnect() {
        sendByeMessage();
    }

    @Override
    public void disconnect() {
        closeSocket();
        System.out.println("Client socket closed");
        System.exit(0);
    }

    private DatagramSocket createClientSocket() {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(SO_TIMEOUT_MS);
            return clientSocket;
        } catch (SocketException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void closeSocket() {
        clientSocket.close();
    }

    private InetSocketAddress createServerSocketAddress(String hostname, int port) {
        try {
            return new InetSocketAddress(
                    InetAddress.getByName(hostname),
                    port
            );
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSenderName() {
        return senderName;
    }


    public long getUID() {
        return UID;
    }

    @Override
    public void setUID(long UID) {
        this.UID = UID;
    }

    public InetSocketAddress getServerSocketAddress() {
        return serverSocketAddress;
    }
}
