package nhz.http;

import nhz.NhzException;
import nhz.util.Convert;
import nhz.xbridge.chat.Chat;
import nhz.xbridge.chat.Message;
import nhz.xbridge.chat.MessageRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReceiveXBridgeMessages extends APIServlet.APIRequestHandler {
    static final ReceiveXBridgeMessages instance = new ReceiveXBridgeMessages();
    private static Set<String> accounts = new HashSet<>();

    private ReceiveXBridgeMessages() {
        super(new APITag[]{APITag.XBRIDGE}, "xBridge");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest request) throws NhzException {
        String accountId = Convert.emptyToNull(request.getParameter("accountId"));
        if(!accounts.contains(accountId)) {
            accounts.add(accountId);

            try {
                Chat.getInstance().initListener(accountId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        MessageRepository repository = MessageRepository.getInstance();
        List<Message> notSeen = repository.getNotSeen(accountId);

        JSONObject response = new JSONObject();
        JSONArray msgs = new JSONArray();
        for(Message message: notSeen) {
            JSONObject msg = new JSONObject();
            msg.put("from", message.getFrom());
            msg.put("to", message.getTo());
            msg.put("content", message.getContent());
            msg.put("dateTime", message.getDateTime().getTime());
            msgs.add(msg);

            message.setSeen(true);
        }
        response.put("msgs", msgs);

        return response;
    }
}
