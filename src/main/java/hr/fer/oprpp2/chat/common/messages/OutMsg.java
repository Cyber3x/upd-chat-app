package hr.fer.oprpp2.chat.common.messages;

import java.io.*;

public class OutMsg {
    public static final byte MESSAGE_TYPE_ID = 4;

    private final byte messageTypeId;
    private final long number;
    private final String senderName;
    private final String messageText;

    public OutMsg(long number, String senderName, String messageText) {
        this.messageTypeId = MESSAGE_TYPE_ID;
        this.number = number;
        this.senderName = senderName;
        this.messageText = messageText;
    }

    public static OutMsg fromBytes(byte[] input) {
        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(input);
                DataInputStream dis = new DataInputStream(bis);
        ) {
            byte messageTypeId = dis.readByte();
            long number = dis.readLong();
            String senderName = dis.readUTF();
            String messageText = dis.readUTF();
            return new OutMsg(number, senderName, messageText);
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
        return String.format("OUTMSG %d from: %s, message: %s", this.number, this.senderName, this.messageText);
    }
}
