package nhz.http;

import nhz.NhzException;
import nhz.Trade;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

public final class GetAllTrades extends APIServlet.APIRequestHandler {

    static final GetAllTrades instance = new GetAllTrades();

    private GetAllTrades() {
        super(new APITag[] {APITag.AE}, "timestamp");
    }
    
    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws NhzException {
        int timestamp = ParameterParser.getTimestamp(req);
        JSONObject response = new JSONObject();
        JSONArray tradesData = new JSONArray();
        Collection<List<Trade>> trades = Trade.getAllTrades();
        for (List<Trade> assetTrades : trades) {
            for (Trade trade : assetTrades) {
                if (trade.getTimestamp() >= timestamp) {
                    tradesData.add(JSONData.trade(trade));
                }
            }
        }
        response.put("trades", tradesData);
        return response;
    }

}
