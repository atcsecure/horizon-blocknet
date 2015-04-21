package nhz.http;

import nhz.NhzException;
import nhz.Order;
import nhz.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAccountCurrentAskOrderIds extends APIServlet.APIRequestHandler {

    static final GetAccountCurrentAskOrderIds instance = new GetAccountCurrentAskOrderIds();

    private GetAccountCurrentAskOrderIds() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.AE}, "account", "asset");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws NhzException {

        Long accountId = ParameterParser.getAccount(req).getId();
        Long assetId = null;
        try {
            assetId = Convert.parseUnsignedLong(req.getParameter("asset"));
        } catch (RuntimeException e) {
            // ignore
        }

        JSONArray orderIds = new JSONArray();
        for (Order.Ask askOrder : Order.Ask.getAllAskOrders()) {
            if ((assetId == null || askOrder.getAssetId().equals(assetId)) && askOrder.getAccount().getId().equals(accountId)) {
                orderIds.add(Convert.toUnsignedLong(askOrder.getId()));
            }
        }

        JSONObject response = new JSONObject();
        response.put("askOrderIds", orderIds);
        return response;
    }

}
