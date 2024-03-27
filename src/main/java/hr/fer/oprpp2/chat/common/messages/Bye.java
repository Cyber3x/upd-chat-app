package hr.fer.oprpp2.chat.common.messages;

import java.io.*;

public class Bye {
    public static final byte MESSAGE_TYPE_ID = 3;

    private final byte messageTypeId;
    private final long number;
    private final long UID;

    public Bye(long number, long UID) {
        this.messageTypeId = MESSAGE_TYPE_ID;
        this.number = number;
        this.UID = UID;
    }

    public static Bye fromBytes(byte[] input) {
        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(input);
                DataInputStream dis = new DataInputStream(bis);
        ) {
            byte messageTypeId = dis.readByte();
            long number = dis.readLong();
            long UID = dis.readLong();
            return new Bye(number, UID);
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
            dos.writeLong(this.UID);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return String.format("BYE %d: UID: %d", this.number, this.UID);
    }

    public byte getMessageTypeId() {
        return messageTypeId;
    }

    public long getNumber() {
        return number;
    }

    public long getUID() {
        return UID;
    }
}
