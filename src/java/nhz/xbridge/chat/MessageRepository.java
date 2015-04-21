package nhz.xbridge.chat;

import nhz.xbridge.XBridgeMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    private static class MessageRepositaryHolder {
        public static final MessageRepository INSTANCE = new MessageRepository();
    }

    public static MessageRepository getInstance() {
        return MessageRepositaryHolder.INSTANCE;
    }

    private MessageRepository() {

    }

    private List<Message> messages = new ArrayList<>();

    public void add(Message message) {
        messages.add(message);
    }

    public List<Message> getNotSeen(String to) {
        List<Message> result = new ArrayList<>();
        for(Message message: messages) {
            if(!message.isSeen() && to.equals(message.getTo()))
                result.add(message);
        }
        return result;
    }

    public void add(XBridgeMessage xBridgeMessage) {
        add(new Message(xBridgeMessage));
    }

    public List<Message> getMessages() {
        return messages;
    }

}
