package nhz.http;

import nhz.NhzException;
import nhz.Order;
import nhz.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

public final class GetAskOrderIds extends APIServlet.APIRequestHandler {

    static final GetAskOrderIds instance = new GetAskOrderIds();

    private GetAskOrderIds() {
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

        JSONArray orderIds = new JSONArray();
        Iterator<Order.Ask> askOrders = Order.Ask.getSortedOrders(assetId).iterator();
        while (askOrders.hasNext() && limit-- > 0) {
            orderIds.add(Convert.toUnsignedLong(askOrders.next().getId()));
        }

        JSONObject response = new JSONObject();
        response.put("askOrderIds", orderIds);
        return response;

    }

}
