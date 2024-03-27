package hr.fer.oprpp2.chat.common.messages;

import java.io.*;

public class Hello extends Message {
    public final static byte MESSAGE_TYPE_ID = 1;

    private long messageNumber;
    private String senderName;
    private long randKey;

    public Hello(long messageNumber, String senderName, long randKey) {
        this.messageNumber = messageNumber;
        this.senderName = senderName;
        this.randKey = randKey;
    }

    public Hello(byte[] data) {
        fillFromBytes(data);
    }

    @Override
    void readDataFromStream(DataInputStream dataInputStream) throws IOException {
        messageNumber = dataInputStream.readLong();
        senderName = dataInputStream.readUTF();
        randKey = dataInputStream.readLong();
    }

    @Override
    public byte getMessageTypeId() {
        return MESSAGE_TYPE_ID;
    }

    @Override
    void writeDataToStream(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeLong(this.messageNumber);
        dataOutputStream.writeUTF(this.senderName);
        dataOutputStream.writeLong(this.randKey);
    }

    @Override
    public String toString() {
        return String.format("[HELLO(%d) senderName = %s, randKey = %d]", this.messageNumber, this.senderName, this.randKey);
    }

    @Override
    public long getMessageNumber() {
        return messageNumber;
    }

    public String getSenderName() {
        return senderName;
    }

    public long getRandKey() {
        return randKey;
    }
}
