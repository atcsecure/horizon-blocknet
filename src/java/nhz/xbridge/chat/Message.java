package nhz.xbridge.chat;

import nhz.xbridge.XBridgeCommand;
import nhz.xbridge.XBridgeMessage;
import nhz.xbridge.XBridgePackage;

import java.nio.ByteBuffer;
import java.util.Date;

public class Message {
    private String from;
    private String to;
    private String content;
    private Date dateTime = new Date();
    private boolean seen = false;

    public Message(XBridgeMessage xBridgeMessage) {
        from = xBridgeMessage.getFrom();
        to = xBridgeMessage.getTo();
        content = new String(xBridgeMessage.getContent());
        dateTime = new Date();
    }

    public Message() {
    }

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public XBridgePackage toXBridgePackage() {
        byte[] toBytes = to.getBytes();
        byte[] fromBytes = from.getBytes();
        byte[] fromContent = content.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(toBytes.length + fromBytes.length + fromContent.length);
        buffer.put(toBytes).put(fromBytes).put(fromContent);
        XBridgePackage xBridgePackage = new XBridgePackage(XBridgeCommand.xbcXChatMessage, buffer.array());
        return xBridgePackage;
    }

    @Override
    public String toString() {
        return "Message{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", content='" + content + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }
}
