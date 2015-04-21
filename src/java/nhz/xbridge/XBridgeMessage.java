package nhz.xbridge;

import java.nio.ByteBuffer;

public class XBridgeMessage {
    public static final int X_BRIDGE_ADDRESS_SIZE_IN_BYTES = 20;
    public static final int X_BRIDGE_INT_SIZE_IN_BYTES = 4;

    private String to;
    private String from;
    private byte[] content;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public XBridgePackage toXBridgePackage() {
        byte[] toBytes = to.getBytes();
        byte[] fromBytes = from.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(toBytes.length + fromBytes.length + content.length);
        buffer.put(toBytes).put(fromBytes).put(content);
        XBridgePackage xBridgePackage = new XBridgePackage(XBridgeCommand.xbcXChatMessage, buffer.array());
        return xBridgePackage;
    }

}
