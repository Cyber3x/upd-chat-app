package hr.fer.oprpp2.chat.client;

import hr.fer.oprpp2.chat.common.messages.Ack;
import hr.fer.oprpp2.chat.common.messages.Bye;
import hr.fer.oprpp2.chat.common.messages.Hello;
import hr.fer.oprpp2.chat.common.messages.OutMsg;

import java.net.*;
import java.util.Random;

import static hr.fer.oprpp2.chat.common.DatagramUtils.sendDatagram;

public class ChatClient {
    private static final int SO_TIMEOUT_MS = 5000;

    private final String senderName;
    private final InetSocketAddress serverSocketAddress;
    private final DatagramSocket clientSocket;

    // this clients UID (User ID)
    private long UID;

    // Keep track of the packet numbering
    private long currentPacketNumber = 0;

    public ChatClient(String hostname, int port, String senderName) {
        this.senderName = senderName;
        this.serverSocketAddress = createServerSocketAddress(hostname, port);
        this.clientSocket = createClientSocket();

        ReceiverThread receiverThread = new ReceiverThread(clientSocket);
        receiverThread.start();
        sendHelloMessage();
    }

    public void sendMessage(String messageText) {
        OutMsg outMsg = new OutMsg(currentPacketNumber, senderName, messageText);
        currentPacketNumber++;

        byte[] outMsgBytes = outMsg.toBytes();

        DatagramPacket receivedPacket = sendDatagram(
                clientSocket,
                serverSocketAddress,
                outMsgBytes,
                new byte[Ack.getByteSize()]
        );
    }

    private void sendHelloMessage() {
        Hello helloMessage = new Hello(currentPacketNumber, senderName, new Random().nextLong());
        currentPacketNumber += 1;

        byte[] helloMessageBytes = helloMessage.toBytes();

        DatagramPacket receivedPacket = sendDatagram(
                clientSocket,
                serverSocketAddress,
                helloMessageBytes,
                new byte[Ack.getByteSize()]
        );

        if (receivedPacket == null) {
            System.out.println("Unable to send hello message, qutting.");
            System.exit(1);
        }

        Ack ack = Ack.fromBytes(receivedPacket.getData());
        if (ack.getNumber() != 0) {
            // todo: move this into the sendDatagram function?
            System.out.println("Got ack that is not 0????");
        }

        UID = ack.getUID();
        System.out.println("my UID is: " + UID);
    }

    public void sendByeMessage() {
        Bye byeMessage = new Bye(currentPacketNumber, UID);
        currentPacketNumber += 1;

        byte[] byeMessageBytes = byeMessage.toBytes();

        DatagramPacket receivedPacket = sendDatagram(
                clientSocket,
                serverSocketAddress,
                byeMessageBytes,
                new byte[Ack.getByteSize()]
        );

        if (receivedPacket == null) {
            System.out.println("Unable to send bye packet, quitting.");
            System.exit(1);
        }

        Ack ack = Ack.fromBytes(receivedPacket.getData());
        if (ack.getNumber() == byeMessage.getNumber()) {
            System.out.println("Ack for bye received, shutting down");
            closeSocket();
        }
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

    public InetSocketAddress getServerSocketAddress() {
        return serverSocketAddress;
    }
}
