package nhz.http;

import nhz.NhzException;
import nhz.Order;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

public final class GetBidOrders extends APIServlet.APIRequestHandler {

    static final GetBidOrders instance = new GetBidOrders();

    private GetBidOrders() {
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
        Iterator<Order.Bid> bidOrders = Order.Bid.getSortedOrders(assetId).iterator();
        while (bidOrders.hasNext() && limit-- > 0) {
            orders.add(JSONData.bidOrder(bidOrders.next()));
        }

        JSONObject response = new JSONObject();
        response.put("bidOrders", orders);
        return response;
    }

}
