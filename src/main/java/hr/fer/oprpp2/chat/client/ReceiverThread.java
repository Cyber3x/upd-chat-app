package hr.fer.oprpp2.chat.client;

import hr.fer.oprpp2.chat.common.messages.Ack;
import hr.fer.oprpp2.chat.common.messages.Bye;
import hr.fer.oprpp2.chat.common.messages.InMsg;
import hr.fer.oprpp2.chat.common.SentMessageMetadata;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;

public class ReceiverThread extends Thread {
    private final DatagramSocket clientSocket;
    private final ConcurrentHashMap<Long, SentMessageMetadata> sentMessagesWaitingForAck;
    private final IChatClient chatClient;

    public ReceiverThread(IChatClient chatClient, DatagramSocket clientSocket, ConcurrentHashMap<Long, SentMessageMetadata> sendMessagesWaitingForAck) {
        this.chatClient = chatClient;
        this.clientSocket = clientSocket;
        this.sentMessagesWaitingForAck = sendMessagesWaitingForAck;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        byte[] receiveBuffer;
        DatagramPacket receivePacket;

        while (true) {
            receiveBuffer = new byte[128];

            receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            try {
                clientSocket.receive(receivePacket);
            } catch (SocketTimeoutException ste) {
                continue;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            byte packetType = receivePacket.getData()[0];
            switch (packetType) {
                case Ack.MESSAGE_TYPE_ID -> {
                    Ack ack = new Ack(receivePacket.getData());
                    long messageNumber = ack.getMessageNumber();

                    SentMessageMetadata sentMessageMetadata = this.sentMessagesWaitingForAck.get(messageNumber);
                    System.out.printf("ack for message number %d received. Was msg waiting for ack?: %s\n", messageNumber, sentMessageMetadata != null);

                    if (sentMessageMetadata != null) {
                        this.sentMessagesWaitingForAck.remove(messageNumber);

                        if (sentMessageMetadata.message().getMessageTypeId() == Bye.MESSAGE_TYPE_ID) {
                            System.out.println("DISCONNECT");
                            chatClient.disconnect();
                        }
                    }

                    if (messageNumber == 0) {
                        // the ACK with number 0 will be the ack to the hello message, thus we can set the UID here.
                        this.chatClient.setUID(ack.getUID());
                    }
                }
                case InMsg.MESSAGE_TYPE_ID -> {
                    InMsg inMsg = new InMsg(receivePacket.getData());
                    System.out.printf("received a message from: %s, with message number: %d\n", inMsg.getSenderName(), inMsg.getMessageNumber());
                    chatClient.handleIncomingMessage(inMsg);
                }
                default -> System.out.printf("got packet with type: %d, currently not supported\n", packetType);
            }
        }
    }
}
