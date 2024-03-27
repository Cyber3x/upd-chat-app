package hr.fer.oprpp2.chat.common.messages;

import java.io.*;
import java.util.Objects;

public final class Ack {
    public static final byte MESSAGE_TYPE_ID = 2;

    private final byte messageTypeId;
    private final long number;
    private final long UID;

    public Ack(long number, long UID) {
        this.messageTypeId = MESSAGE_TYPE_ID;
        this.number = number;
        this.UID = UID;
    }

    public static Ack fromBytes(byte[] input) {
        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(input);
                DataInputStream dis = new DataInputStream(bis);
        ) {
            byte messageTypeId = dis.readByte();
            long number = dis.readLong();
            long UID = dis.readLong();
            return new Ack(number, UID);
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

    public static int getByteSize() {
        // BYTE 1 + LONG 8 + LONG 8
        return 1 + 8 + 8;
    }

    @Override
    public String toString() {
        return String.format("ACK %d: UID: %d", this.number, this.UID);
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
