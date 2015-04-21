package nhz.http;

import nhz.NhzException;
import nhz.Order;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

public final class GetAskOrders extends APIServlet.APIRequestHandler {

    static final GetAskOrders instance = new GetAskOrders();

    private GetAskOrders() {
        super(new APITag[] {APITag.AE}, "asset", "limit");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws NhzException {

        Long assetId = ParameterParser.getAsset(req).getId();

        int limit;
        try {
            limit = Integer.parseInt(req.getParameter("limit"));
        } catch (NumberFormatException e) {
            limit = Integer.MAX_VALUE;
        }

        JSONArray orders = new JSONArray();
        Iterator<Order.Ask> askOrders = Order.Ask.getSortedOrders(assetId).iterator();
        while (askOrders.hasNext() && limit-- > 0) {
            orders.add(JSONData.askOrder(askOrders.next()));
        }

        JSONObject response = new JSONObject();
        response.put("askOrders", orders);
        return response;

    }

}
