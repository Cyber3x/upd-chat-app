package hr.fer.oprpp2.chat.server;

import hr.fer.oprpp2.chat.common.messages.Ack;
import hr.fer.oprpp2.chat.common.messages.Bye;
import hr.fer.oprpp2.chat.common.messages.Hello;
import hr.fer.oprpp2.chat.common.messages.OutMsg;

import java.io.IOException;
import java.net.*;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ChatServer {
    private final DatagramSocket serverSocket;

    private long UIDCounter = new Random().nextLong();

    private final ConnectionsStorage connectionsStorage = new ConnectionsStorage();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ChatServer(int port) {
        serverSocket = createServerSocket(port);

        System.out.println("Server running on port: " + port);
        listenForConnections();
    }

    private void listenForConnections() {
        byte[] receiveBuffer;
        DatagramPacket receivePacket;

        while (true) {
            receiveBuffer = new byte[128];
            receivePacket = new DatagramPacket(
                    receiveBuffer, receiveBuffer.length
            );

            try {
                serverSocket.receive(receivePacket);
                parseIncomingPacket(receivePacket);
            } catch (IOException e) {
                System.err.println("error while listening for a connection. Will continue. " + e.getMessage());
            }
        }
    }

    private void parseIncomingPacket(DatagramPacket incomingPacket) {

        byte[] incomingPacketData = incomingPacket.getData();
        byte packetType = incomingPacketData[0];

        SocketAddress incomingPacketAddress = incomingPacket.getSocketAddress();
        System.out.printf("\nReceived packet from: %s\nPacket: ", incomingPacketAddress);

        switch (packetType) {
            case Hello.MESSAGE_TYPE_ID -> {
                Hello helloMessage = new Hello(incomingPacketData);
                System.out.println(helloMessage);

                Optional<ConnectionDescriptor> connectionDescriptor = connectionsStorage.getConnectionDescriptorByRandKey(incomingPacketAddress, helloMessage.getRandKey());

                Ack ack;
                if (connectionDescriptor.isEmpty()) {
                    long UID = getNextUID();
                    ack = new Ack(helloMessage.getMessageNumber(), UID);

                    ConnectionThread connectionThread = new ConnectionThread(serverSocket, incomingPacketAddress, helloMessage.getSenderName(), scheduler);
                    connectionThread.setDaemon(true);
                    connectionThread.start();

                    ConnectionDescriptor newConnectionDescriptor = new ConnectionDescriptor(helloMessage.getRandKey(), UID, helloMessage.getSenderName(), connectionThread);
                    connectionsStorage.storeConnection(incomingPacketAddress, newConnectionDescriptor);
                } else {
                    ack = new Ack(helloMessage.getMessageNumber(), connectionDescriptor.orElseThrow().UID());
                }

                sendAck(ack, (InetSocketAddress) incomingPacketAddress);
            }
            case Ack.MESSAGE_TYPE_ID -> {
                Ack ack = new Ack(incomingPacketData);
                System.out.println(ack);

                ConnectionDescriptor connectionDescriptor = connectionsStorage.getConnectionDescriptorByUID(incomingPacketAddress, ack.getUID()).orElseThrow();
                connectionDescriptor.connectionThread().addMessage(ack);
            }
            case Bye.MESSAGE_TYPE_ID -> {
                Bye bye = new Bye(incomingPacketData);
                System.out.println(bye);

                Ack ack = new Ack(bye.getMessageNumber(), bye.getUID());
                sendAck(ack, (InetSocketAddress) incomingPacketAddress);

                connectionsStorage.getConnectionDescriptorByUID(incomingPacketAddress, bye.getUID())
                        .ifPresent(
                                (connectionDescriptor) -> {
                                    connectionDescriptor.connectionThread().markForStopping();
                                    connectionDescriptor.connectionThread().interrupt();
                                    connectionsStorage.removeConnection(incomingPacketAddress, connectionDescriptor);
                                });
            }
            case OutMsg.MESSAGE_TYPE_ID -> {
                OutMsg outMsg = new OutMsg(incomingPacketData);
                System.out.println(outMsg);

                Ack ack = new Ack(outMsg.getMessageNumber(), outMsg.getUID());
                sendAck(ack, (InetSocketAddress) incomingPacketAddress);

                ConnectionDescriptor connectionDescriptor = connectionsStorage.getConnectionDescriptorByUID(incomingPacketAddress, outMsg.getUID()).orElseThrow();
                outMsg.setSenderName(connectionDescriptor.clientName());

                for (ConnectionDescriptor targetConnectionDescriptor : connectionsStorage.getAllConnectionDescriptors()) {
                    targetConnectionDescriptor.connectionThread().addMessage(outMsg);
                }
            }
            default -> {
                System.err.println("Unsupported package type received");
            }
        }
    }

    private void sendAck(Ack ack, InetSocketAddress destinationAddress) {
        try {
            serverSocket.send(ack.toPacket(destinationAddress));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long getNextUID() {
        return UIDCounter++;
    }

    private DatagramSocket createServerSocket(int port) {
        try {
            return new DatagramSocket(new InetSocketAddress(port));
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
}
