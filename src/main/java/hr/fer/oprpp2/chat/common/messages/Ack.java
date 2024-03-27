package hr.fer.oprpp2.chat.common.messages;

import java.io.*;

public final class Ack extends Message {
    public static final byte MESSAGE_TYPE_ID = 2;

    private long messageNumber;
    private long UID;

    public Ack(long number, long UID) {
        this.messageNumber = number;
        this.UID = UID;
    }

    public Ack(byte[] data) {
        this.fillFromBytes(data);
    }

    @Override
    void readDataFromStream(DataInputStream dataInputStream) throws IOException {
        this.messageNumber = dataInputStream.readLong();
        this.UID = dataInputStream.readLong();
    }

    @Override
    void writeDataToStream(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeLong(this.messageNumber);
        dataOutputStream.writeLong(this.UID);
    }

    public static int getByteSize() {
        // BYTE 1 + LONG 8 + LONG 8
        return 1 + 8 + 8;
    }

    @Override
    public String toString() {
        return String.format("[ACK(%d) UID = %d]", this.messageNumber, this.UID);
    }

    public byte getMessageTypeId() {
        return MESSAGE_TYPE_ID;
    }

    public long getMessageNumber() {
        return messageNumber;
    }

    public long getUID() {
        return UID;
    }
}
