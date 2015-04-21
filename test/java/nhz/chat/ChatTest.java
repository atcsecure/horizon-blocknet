package nhz.chat;

import nhz.util.Logger;
import nhz.xbridge.chat.Chat;
import nhz.xbridge.chat.Message;
import nhz.xbridge.chat.MessageRepository;
import org.junit.Test;

import java.util.List;

public class ChatTest {
    private static final String SENDER = "604be45e-49ad-4daf-97bc-e9d6cb145f66";
    private static final String RECEIVER = "7ef6f556-e829-11e4-b02c-1681e6b88ec1";

    public ChatTest() throws Exception {
        Chat.getInstance().initListener(RECEIVER);
    }

    @Test
    public void testSend() throws Exception {
        Chat chat = Chat.getInstance();

        Message message = new Message();
        message.setFrom(SENDER);
        message.setTo(RECEIVER);
        message.setContent("Hello World!!!");

        chat.send(message);
    }

    @Test
    public void testReceive() throws Exception {
        MessageRepository repository = MessageRepository.getInstance();

        List<Message> notSeen = repository.getNotSeen(RECEIVER);
        Logger.logDebugMessage("testReceive (" + RECEIVER + "): " + notSeen);
    }
}
