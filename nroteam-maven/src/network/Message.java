package network;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;

import interfaces.IMessage;
import utils.Util;

public class Message implements IMessage {

    public byte command;
    private ByteArrayOutputStream os;
    private DataOutputStream dos;
    private ByteArrayInputStream is;
    private DataInputStream dis;

    public Message(int command) {
        this((byte) command);
    }

    public Message(byte command) {
        this.command = command;
        this.os = new ByteArrayOutputStream();
        this.dos = new DataOutputStream(this.os);
    }

    public Message(byte command, byte[] data) {
        this.command = command;
        this.is = new ByteArrayInputStream(data);
        this.dis = new DataInputStream(this.is);
    }
    /** Payload size in bytes (set for inbound messages, 0 for outbound). */
    private int dataSize;

    public int getDataSize() { return dataSize; }

    // Replaces the plain constructor to track payload size for Stage-2 filtering
    public Message(byte command, byte[] data, int size) {
        this(command, data);
        this.dataSize = size;
    }

    @Override
    public DataOutputStream writer() {
        return this.dos;
    }

    @Override
    public DataInputStream reader() {
        return this.dis;
    }

    @Override
    public byte[] getData() {
        return this.os.toByteArray();
    }

    @Override
    public void cleanup() {
        try {
            if (this.is != null) {
                this.is.close();
            }
            if (this.os != null) {
                this.os.close();
            }
            if (this.dis != null) {
                this.dis.close();
            }
            if (this.dos != null) {
                this.dos.close();
            }
        } catch (IOException e) {
        }
    }

    @Override
    public void dispose() {
        this.cleanup();
        this.dis = null;
        this.is = null;
        this.dos = null;
        this.os = null;
    }

    public void writeSmartLong(double value) throws IOException {
        if (Util.readInt) {
            if (value > Integer.MAX_VALUE) {
                writer().writeInt(Integer.MAX_VALUE);
            } else if (value < Integer.MIN_VALUE) {
                writer().writeInt(Integer.MIN_VALUE);
            } else {
                writer().writeInt((int) value);
            }
        } else {
            writer().writeLong((long) value);
        }
    }

    public String readUTF() throws IOException {
        if (dis == null) {
            throw new IllegalStateException("Message not in read mode");
        }
        return dis.readUTF();
    }
}
