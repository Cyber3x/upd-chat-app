package hr.fer.oprpp2.chat.common.messages;

import java.io.*;

public class Hello {
    public static final byte MESSAGE_TYPE_ID = 1;

    private final long number;
    private final String senderName;
    private final long randKey;

    public Hello(long number, String senderName, long randKey) {
        this.number = number;
        this.senderName = senderName;
        this.randKey = randKey;
    }

    public static Hello fromBytes(byte[] input) {
        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(input);
                DataInputStream dis = new DataInputStream(bis);
        ) {
            byte messageTypeId = dis.readByte();
            long number = dis.readLong();
            String senderName = dis.readUTF();
            long randKey = dis.readLong();
            return new Hello(number, senderName, randKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public byte[] toBytes() {
        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
        ) {
            dos.writeByte(MESSAGE_TYPE_ID);
            dos.writeLong(this.number);
            dos.writeUTF(this.senderName);
            dos.writeLong(this.randKey);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return String.format("HELLO %d from: %s, randKey: %d", this.number, this.senderName, this.randKey);
    }
}
