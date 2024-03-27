package hr.fer.oprpp2.chat.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReceiverThread extends Thread {
    private final DatagramSocket clientSocket;

    public ReceiverThread(DatagramSocket clientSocket) {
        this.clientSocket = clientSocket;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        byte[] receiveBuffer = new byte[128];
        DatagramPacket receivePacket = new DatagramPacket(
                receiveBuffer, receiveBuffer.length
        );

        while (true) {
            try {
                clientSocket.receive(receivePacket);
                System.out.println("second thread got packet");
                clientSocket.
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
