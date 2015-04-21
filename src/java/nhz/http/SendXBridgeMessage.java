package nhz.http;

import nhz.NhzException;
import nhz.util.Logger;
import nhz.xbridge.chat.Chat;
import nhz.xbridge.chat.Message;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class SendXBridgeMessage extends APIServlet.APIRequestHandler {
    static final SendXBridgeMessage instance = new SendXBridgeMessage();

    private SendXBridgeMessage() {
        super(new APITag[]{APITag.XBRIDGE}, "xBridge");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest request) throws NhzException {
        Chat chat = Chat.getInstance();
        Message message = ParameterParser.getSendingXBridgeMessage(request);

        try {
            chat.send(message);
        } catch (IOException e) {
            Logger.logDebugMessage("Error processing API request", e);
            throw new RuntimeException(e);
        }

        JSONObject response = new JSONObject();
        response.put("ok", true);
        return response;
    }
}
