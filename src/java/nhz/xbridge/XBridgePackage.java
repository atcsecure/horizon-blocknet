package nhz.xbridge;

import java.nio.ByteBuffer;

public class XBridgePackage {
    private XBridgeCommand xBridgeCommand = XBridgeCommand.xbcAnnounceAddresses;
    private byte[] data;

    public XBridgePackage(XBridgeCommand xBridgeCommand, byte[] data) {
        this.xBridgeCommand = xBridgeCommand;
        this.data = data;
    }

    public XBridgeCommand getxBridgeCommand() {
        return xBridgeCommand;
    }

    public void setxBridgeCommand(XBridgeCommand xBridgeCommand) {
        this.xBridgeCommand = xBridgeCommand;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] convertToByteArray() {
        int command = xBridgeCommand.ordinal();
        int size = 0;
        if (null != data)
            size = data.length;

        ByteBuffer buffer = ByteBuffer.allocate(XBridgeMessage.X_BRIDGE_INT_SIZE_IN_BYTES + XBridgeMessage.X_BRIDGE_INT_SIZE_IN_BYTES + size);
        buffer.put(inverse(intToByteArray(command)));
        buffer.put(inverse(intToByteArray(size)));
        buffer.put(data);

        return buffer.array();
    }

    private byte[] intToByteArray(int i) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(i);
        return b.array();
    }

    private byte[] inverse(byte[] bytes) {
        byte[] result = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = bytes[bytes.length - 1 - i];
        }
        return result;
    }
}
