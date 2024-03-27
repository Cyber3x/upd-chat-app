package hr.fer.oprpp2.chat.common.messages;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public abstract class Message {
    public void fillFromBytes(byte[] inputData) {
        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(inputData);
                DataInputStream dis = new DataInputStream(bis);
        ) {
            byte messageTypeId = dis.readByte();
            if (messageTypeId != this.getMessageTypeId()) {
                throw new IllegalArgumentException("Unable to parse packet with ID: " + messageTypeId);
            }
            readDataFromStream(dis);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert this object to a byte array.
     * @return byte array with this object's data.
     */
    public byte[] toBytes() {
        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
        ) {
            dos.writeByte(this.getMessageTypeId());
            writeDataToStream(dos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A method where you define what is written into the stream that's going to be converted to a byte array.
     * Similar to serializing an object.
     * @param dataOutputStream the target output stream
     * @throws IOException
     */
    abstract void writeDataToStream(DataOutputStream dataOutputStream) throws IOException;

    /**
     * A method where you can read from the input stream in order to extract information for this object.
     * Similar to deserializing an object.
     *
     * @param dataInputStream the input stream with the information
     * @throws IOException
     */
    abstract void readDataFromStream(DataInputStream dataInputStream) throws IOException;

    public DatagramPacket toPacket(InetSocketAddress destinationAddress) {
        byte[] dataArray = this.toBytes();
        return new DatagramPacket(
                dataArray, 0, dataArray.length, destinationAddress
        );
    }

    abstract public byte getMessageTypeId();

    abstract public long getMessageNumber();
}
