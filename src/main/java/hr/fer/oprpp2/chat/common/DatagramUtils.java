package hr.fer.oprpp2.chat.common;

import java.io.IOException;
import java.net.*;

public class DatagramUtils {
    private static final int PACKET_RESEND_COUNT = 10;
    
    public static DatagramPacket sendDatagram(
            DatagramSocket senderSocket, 
            InetSocketAddress destinationAddress,
            byte[] data, 
            byte[] receiveBuffer
    ) {
        DatagramPacket packet = new DatagramPacket(
                data, 0, data.length, destinationAddress
        );

        DatagramPacket receivePacket = new DatagramPacket(
                receiveBuffer, receiveBuffer.length
        );

        int packetResendCountLeft = PACKET_RESEND_COUNT;

        boolean answerReceived = false;

        while (true) {
            try {
                System.out.println("sending packet");
                senderSocket.send(packet);

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("can't send packet, quitting");
                break;
            }

            try {
                System.out.println("waiting for ack...");
                senderSocket.receive(receivePacket);
                answerReceived = true;
                break;
            } catch (SocketTimeoutException socketTimeoutException) {
                System.out.println("waiting timed out");
                packetResendCountLeft -= 1;
                if (packetResendCountLeft > 0) {
                    System.out.println("sending packet again, retires left: " + packetResendCountLeft);
                } else {
                    break;
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                break;
            }
        }

        if (!answerReceived) {
            System.out.println("Answer not received after transmissions, check the server");
            return null;
        }

        return receivePacket;
    }
}
