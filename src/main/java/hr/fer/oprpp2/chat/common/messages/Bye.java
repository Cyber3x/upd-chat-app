package hr.fer.oprpp2.chat.common.messages;

import java.io.*;

public class Bye extends Message {
    public static final byte MESSAGE_TYPE_ID = 3;

    private long messageNumber;
    private long UID;

    public Bye(long number, long UID) {
        this.messageNumber = number;
        this.UID = UID;
    }

    public Bye(byte[] data) {
        fillFromBytes(data);
    }

    @Override
    void writeDataToStream(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeLong(this.messageNumber);
        dataOutputStream.writeLong(this.UID);
    }

    @Override
    void readDataFromStream(DataInputStream dataInputStream) throws IOException {
        messageNumber = dataInputStream.readLong();
        UID = dataInputStream.readLong();
    }

    @Override
    public String toString() {
        return String.format("[BYE(%d) UID = %d]", this.messageNumber, this.UID);
    }

    public byte getMessageTypeId() {
        return MESSAGE_TYPE_ID;
    }

    @Override
    public long getMessageNumber() {
        return messageNumber;
    }

    public long getUID() {
        return UID;
    }
}
