package nhz.http;

import nhz.Asset;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAllAssets extends APIServlet.APIRequestHandler {

    static final GetAllAssets instance = new GetAllAssets();

    private GetAllAssets() {
        super(new APITag[] {APITag.AE});
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {

        JSONObject response = new JSONObject();
        JSONArray assetsJSONArray = new JSONArray();
        response.put("assets", assetsJSONArray);
        for (Asset asset : Asset.getAllAssets()) {
            assetsJSONArray.add(JSONData.asset(asset));
        }
        return response;
    }

}
