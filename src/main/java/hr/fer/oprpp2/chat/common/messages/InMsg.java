package hr.fer.oprpp2.chat.common.messages;

import java.io.*;

public final class InMsg extends Message {
    public static final byte MESSAGE_TYPE_ID = 5;

    private long messageNumber;
    private String senderName;
    private String messageText;

    public InMsg(long number, String senderName, String messageText) {
        this.messageNumber = number;
        this.senderName = senderName;
        this.messageText = messageText;
    }

    public InMsg(byte[] data) {
        fillFromBytes(data);
    }

    @Override
    void writeDataToStream(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeLong(this.messageNumber);
        dataOutputStream.writeUTF(this.senderName);
        dataOutputStream.writeUTF(this.messageText);
    }

    @Override
    void readDataFromStream(DataInputStream dataInputStream) throws IOException {
        messageNumber = dataInputStream.readLong();
        senderName = dataInputStream.readUTF();
        messageText = dataInputStream.readUTF();
    }


    @Override
    public String toString() {
        return String.format("[INMSG(%d) senderName = %s, messageText = %s]", this.messageNumber, this.senderName, this.messageText);
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    @Override
    public byte getMessageTypeId() {
        return MESSAGE_TYPE_ID;
    }

    @Override
    public long getMessageNumber() {
        return messageNumber;
    }
}
