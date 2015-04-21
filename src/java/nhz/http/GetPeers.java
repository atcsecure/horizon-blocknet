package nhz.http;

import nhz.peer.Peer;
import nhz.peer.Peers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetPeers extends APIServlet.APIRequestHandler {

    static final GetPeers instance = new GetPeers();

    private GetPeers() {
        super(new APITag[] {APITag.INFO}, "active");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {

        boolean active = "true".equalsIgnoreCase(req.getParameter("active"));
        JSONArray peers = new JSONArray();
        for (Peer peer : active ? Peers.getActivePeers() : Peers.getAllPeers()) {
            peers.add(peer.getPeerAddress());
        }

        JSONObject response = new JSONObject();
        response.put("peers", peers);
        return response;
    }

}
