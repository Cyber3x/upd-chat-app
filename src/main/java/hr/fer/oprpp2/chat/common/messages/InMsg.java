package hr.fer.oprpp2.chat.common.messages;

import java.io.*;

public record InMsg(long number, String senderName, String messageText) {
    public static final byte MESSAGE_TYPE_ID = 5;

    public static InMsg fromBytes(byte[] input) {
        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(input);
                DataInputStream dis = new DataInputStream(bis);
        ) {
            byte messageTypeId = dis.readByte();
            if (messageTypeId != MESSAGE_TYPE_ID) {
                throw new RuntimeException("message type id mismatch");
            }
            long number = dis.readLong();
            String senderName = dis.readUTF();
            String messageText = dis.readUTF();
            return new InMsg(number, senderName, messageText);
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
            dos.writeUTF(this.messageText);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return String.format("INMSG %d from: %s, message: %s", this.number, this.senderName, this.messageText);
    }
}
