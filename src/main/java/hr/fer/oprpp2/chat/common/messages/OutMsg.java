package hr.fer.oprpp2.chat.common.messages;

import java.io.*;

public class OutMsg extends Message {
    public static final byte MESSAGE_TYPE_ID = 4;

    private long messageNumber;
    private long UID;
    private String messageText;

    public OutMsg(long number, long UID, String messageText) {
        this.messageNumber = number;
        this.UID = UID;
        this.messageText = messageText;
    }

    public OutMsg(byte[] data) {
        fillFromBytes(data);
    }

    @Override
    void writeDataToStream(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeLong(messageNumber);
        dataOutputStream.writeLong(UID);
        dataOutputStream.writeUTF(messageText);
    }

    @Override
    void readDataFromStream(DataInputStream dataInputStream) throws IOException {
        messageNumber = dataInputStream.readLong();
        UID = dataInputStream.readLong();
        messageText = dataInputStream.readUTF();
    }

    @Override
    public byte getMessageTypeId() {
        return MESSAGE_TYPE_ID;
    }

    @Override
    public long getMessageNumber() {
        return messageNumber;
    }

    @Override
    public String toString() {
        return String.format("[OUTMSG(%d) UID = %d, messageText = %s]", this.messageNumber, this.UID, this.messageText);
    }

    public long getUID() {
        return UID;
    }

    public String getMessageText() {
        return messageText;
    }
}
