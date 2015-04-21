package nhz.peer;

import nhz.Account;
import nhz.BlockchainProcessor;
import nhz.Constants;
import nhz.NhzException;
import nhz.util.Convert;
import nhz.util.CountingInputStream;
import nhz.util.CountingOutputStream;
import nhz.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

final class PeerImpl implements Peer {

    private final String peerAddress;
    private volatile String announcedAddress;
    private volatile int port;
    private volatile boolean shareAddress;
    private volatile Hallmark hallmark;
    private volatile String platform;
    private volatile String application;
    private volatile String version;
    private volatile long adjustedWeight;
    private volatile long blacklistingTime;
    private volatile State state;
    private volatile long downloadedVolume;
    private volatile long uploadedVolume;
    private volatile int lastUpdated;

    PeerImpl(String peerAddress, String announcedAddress) {
        this.peerAddress = peerAddress;
        this.announcedAddress = announcedAddress;
        try {
            this.port = new URL("http://" + announcedAddress).getPort();
        } catch (MalformedURLException ignore) {}
        this.state = State.NON_CONNECTED;
        this.shareAddress = true;
    }

    @Override
    public String getPeerAddress() {
        return peerAddress;
    }

    @Override
    public State getState() {
        return state;
    }

    void setState(State state) {
        if (this.state == state) {
            return;
        }
        if (this.state == State.NON_CONNECTED) {
            this.state = state;
            Peers.notifyListeners(this, Peers.Event.ADDED_ACTIVE_PEER);
        } else if (state != State.NON_CONNECTED) {
            this.state = state;
            Peers.notifyListeners(this, Peers.Event.CHANGED_ACTIVE_PEER);
        }
    }

    @Override
    public long getDownloadedVolume() {
        return downloadedVolume;
    }

    void updateDownloadedVolume(long volume) {
        synchronized (this) {
            downloadedVolume += volume;
        }
        Peers.notifyListeners(this, Peers.Event.DOWNLOADED_VOLUME);
    }

    @Override
    public long getUploadedVolume() {
        return uploadedVolume;
    }

    void updateUploadedVolume(long volume) {
        synchronized (this) {
            uploadedVolume += volume;
        }
        Peers.notifyListeners(this, Peers.Event.UPLOADED_VOLUME);
    }

    @Override
    public String getVersion() {
        return version;
    }

    void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String getApplication() {
        return application;
    }

    void setApplication(String application) {
        this.application = application;
    }

    @Override
    public String getPlatform() {
        return platform;
    }

    void setPlatform(String platform) {
        this.platform = platform;
    }

    @Override
    public String getSoftware() {
        return Convert.truncate(application, "?", 10, false)
                + " (" + Convert.truncate(version, "?", 10, false) + ")"
                + " @ " + Convert.truncate(platform, "?", 10, false);
    }

    @Override
    public boolean shareAddress() {
        return shareAddress;
    }

    void setShareAddress(boolean shareAddress) {
        this.shareAddress = shareAddress;
    }

    @Override
    public String getAnnouncedAddress() {
        return announcedAddress;
    }

    void setAnnouncedAddress(String announcedAddress) {
        String announcedPeerAddress = Peers.normalizeHostAndPort(announcedAddress);
        if (announcedPeerAddress != null) {
            this.announcedAddress = announcedPeerAddress;
            try {
                this.port = new URL("http://" + announcedPeerAddress).getPort();
            } catch (MalformedURLException ignore) {}
        }
    }

    int getPort() {
        return port;
    }

    @Override
    public boolean isWellKnown() {
        return announcedAddress != null && Peers.wellKnownPeers.contains(announcedAddress);
    }

    @Override
    public Hallmark getHallmark() {
        return hallmark;
    }

    @Override
    public int getWeight() {
        if (hallmark == null) {
            return 0;
        }
        Account account = Account.getAccount(hallmark.getAccountId());
        if (account == null) {
            return 0;
        }
        return (int)(adjustedWeight * (account.getBalanceNQT() / Constants.ONE_NHZ) / Constants.MAX_BALANCE_NHZ);
    }

    @Override
    public boolean isBlacklisted() {
        return blacklistingTime > 0 || Peers.knownBlacklistedPeers.contains(peerAddress);
    }

    @Override
    public void blacklist(Exception cause) {
        if (cause instanceof NhzException.NotCurrentlyValidException || cause instanceof BlockchainProcessor.BlockOutOfOrderException) {
            // don't blacklist peers just because a feature is not yet enabled
            // prevents erroneous blacklisting during loading of blockchain from scratch
            return;
        }
        if (! isBlacklisted() && ! (cause instanceof IOException || cause instanceof ParseException)) {
            Logger.logDebugMessage("Blacklisting " + peerAddress + " because of: " + cause.toString());
        }
        blacklist();
    }

    @Override
    public void blacklist() {
        blacklistingTime = System.currentTimeMillis();
        setState(State.NON_CONNECTED);
        Peers.notifyListeners(this, Peers.Event.BLACKLIST);
    }

    @Override
    public void unBlacklist() {
        setState(State.NON_CONNECTED);
        blacklistingTime = 0;
        Peers.notifyListeners(this, Peers.Event.UNBLACKLIST);
    }

    void updateBlacklistedStatus(long curTime) {
        if (blacklistingTime > 0 && blacklistingTime + Peers.blacklistingPeriod <= curTime) {
            unBlacklist();
        }
    }

    @Override
    public void deactivate() {
        setState(State.NON_CONNECTED);
        Peers.notifyListeners(this, Peers.Event.DEACTIVATE);
    }

    @Override
    public void remove() {
        Peers.removePeer(this);
        Peers.notifyListeners(this, Peers.Event.REMOVE);
    }

    @Override
    public int getLastUpdated() {
        return lastUpdated;
    }

    void setLastUpdated(int lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public JSONObject send(final JSONStreamAware request) {
    	return send(request, Peers.MAX_RESPONSE_SIZE);
    }
    
    @Override
    public JSONObject send(final JSONStreamAware request, int maxResponseSize) {
    	
        JSONObject response = null;

        String log = null;
        boolean showLog = false;
        HttpURLConnection connection = null;

        try {

            String address = announcedAddress != null ? announcedAddress : peerAddress;
            URL url = new URL("http://" + address + (port <= 0 ? ":" + (Constants.isTestnet ? Peers.TESTNET_PEER_PORT : Peers.DEFAULT_PEER_PORT) : "") + "/nhz");

            if (Peers.communicationLoggingMask != 0) {
                StringWriter stringWriter = new StringWriter();
                request.writeJSONString(stringWriter);
                log = "\"" + url.toString() + "\": " + stringWriter.toString();
            }

            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(Peers.connectTimeout);
            connection.setReadTimeout(Peers.readTimeout);
            connection.setRequestProperty("Accept-Encoding", "gzip");

            CountingOutputStream cos = new CountingOutputStream(connection.getOutputStream());
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(cos, "UTF-8"))) {
                request.writeJSONString(writer);
            }
            updateUploadedVolume(cos.getCount());

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            	if (maxResponseSize > 0) {
		            CountingInputStream cis = new CountingInputStream(connection.getInputStream(), maxResponseSize);
		            InputStream responseStream = cis;
		            if ("gzip".equals(connection.getHeaderField("Content-Encoding"))) {
		                responseStream = new GZIPInputStream(cis);
		            }
		            if ((Peers.communicationLoggingMask & Peers.LOGGING_MASK_200_RESPONSES) != 0) {
		                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		                byte[] buffer = new byte[1024];
		                int numberOfBytes;
		                try (InputStream inputStream = responseStream) {
		                    while ((numberOfBytes = inputStream.read(buffer, 0, buffer.length)) > 0) {
		                        byteArrayOutputStream.write(buffer, 0, numberOfBytes);
		                    }
		                }
		                String responseValue = byteArrayOutputStream.toString("UTF-8");
		                if (responseValue.length() > 0 && responseStream instanceof GZIPInputStream) {
		                    log += String.format("[length: %d, compression ratio: %.2f]", cis.getCount(), (double) cis.getCount() / (double) responseValue.length());
		                }
		                log += " >>> " + responseValue;
		                showLog = true;
		                response = (JSONObject) JSONValue.parse(responseValue);
		            } else {
		                try (Reader reader = new BufferedReader(new InputStreamReader(responseStream, "UTF-8"))) {
		                    response = (JSONObject) JSONValue.parseWithException(reader);
		                }
		            }
		            updateDownloadedVolume(cis.getCount());
            	}
            } else {

                if ((Peers.communicationLoggingMask & Peers.LOGGING_MASK_NON200_RESPONSES) != 0) {
                    log += " >>> Peer responded with HTTP " + connection.getResponseCode() + " code!";
                    showLog = true;
                }
                if (state == State.CONNECTED) {
                    setState(State.DISCONNECTED);
                } else {
                    setState(State.NON_CONNECTED);
                }
                response = null;

            }

        } catch (RuntimeException|ParseException e) {
            blacklist(e);
        } catch (IOException e) {
            if (! (e instanceof UnknownHostException || e instanceof SocketTimeoutException || e instanceof SocketException)) {
                Logger.logDebugMessage("Error sending JSON request", e);
            }
            if ((Peers.communicationLoggingMask & Peers.LOGGING_MASK_EXCEPTIONS) != 0) {
                log += " >>> " + e.toString();
                showLog = true;
            }
            if (state == State.CONNECTED) {
                setState(State.DISCONNECTED);
            }
        }

        if (showLog) {
            Logger.logMessage(log + "\n");
        }

        if (connection != null) {
            connection.disconnect();
        }

        return response;

    }

    @Override
    public int compareTo(Peer o) {
        if (getWeight() > o.getWeight()) {
            return -1;
        } else if (getWeight() < o.getWeight()) {
            return 1;
        }
        return 0;
    }

    void connect() {
        JSONObject response = send(Peers.myPeerInfoRequest);
        if (response != null) {
            application = (String)response.get("application");
            version = (String)response.get("version");
            platform = (String)response.get("platform");
            shareAddress = Boolean.TRUE.equals(response.get("shareAddress"));
            String newAnnouncedAddress = Convert.emptyToNull((String)response.get("announcedAddress"));
            if (newAnnouncedAddress != null && ! newAnnouncedAddress.equals(announcedAddress)) {
                // force verification of changed announced address
                setState(Peer.State.NON_CONNECTED);
                setAnnouncedAddress(newAnnouncedAddress);
                return;
            }
            if (announcedAddress == null) {
                setAnnouncedAddress(peerAddress);
                Logger.logDebugMessage("Connected to peer without announced address, setting to " + peerAddress);
            }
            if (analyzeHallmark(announcedAddress, (String)response.get("hallmark")) && version!=null && version.startsWith("NHZ")) {
                setState(State.CONNECTED);
                Peers.updateAddress(this);
            } else {
                blacklist();
            }
            lastUpdated = Convert.getEpochTime();
        } else {
            setState(State.NON_CONNECTED);
        }
    }

    boolean analyzeHallmark(String address, final String hallmarkString) {

        if (hallmarkString == null && this.hallmark == null) {
            return true;
        }

        if (this.hallmark != null && this.hallmark.getHallmarkString().equals(hallmarkString)) {
            return true;
        }

        if (hallmarkString == null) {
            this.hallmark = null;
            return true;
        }

        try {
            URI uri = new URI("http://" + address.trim());
            String host = uri.getHost();

            Hallmark hallmark = Hallmark.parseHallmark(hallmarkString);
            if (!hallmark.isValid()
                    || !(hallmark.getHost().equals(host) || InetAddress.getByName(host).equals(InetAddress.getByName(hallmark.getHost())))) {
                //Logger.logDebugMessage("Invalid hallmark for " + host + ", hallmark host is " + hallmark.getHost());
                return false;
            }
            this.hallmark = hallmark;
            Long accountId = Account.getId(hallmark.getPublicKey());
            List<PeerImpl> groupedPeers = new ArrayList<>();
            int mostRecentDate = 0;
            long totalWeight = 0;
            for (PeerImpl peer : Peers.allPeers) {
                if (peer.hallmark == null) {
                    continue;
                }
                if (accountId.equals(peer.hallmark.getAccountId())) {
                    groupedPeers.add(peer);
                    if (peer.hallmark.getDate() > mostRecentDate) {
                        mostRecentDate = peer.hallmark.getDate();
                        totalWeight = peer.getHallmarkWeight(mostRecentDate);
                    } else {
                        totalWeight += peer.getHallmarkWeight(mostRecentDate);
                    }
                }
            }

            for (PeerImpl peer : groupedPeers) {
                peer.adjustedWeight = Constants.MAX_BALANCE_NHZ * peer.getHallmarkWeight(mostRecentDate) / totalWeight;
                Peers.notifyListeners(peer, Peers.Event.WEIGHT);
            }

            return true;

        } catch (UnknownHostException ignore) {
        } catch (URISyntaxException | RuntimeException e) {
            Logger.logDebugMessage("Failed to analyze hallmark for peer " + address + ", " + e.toString());
        }
        return false;

    }

    private int getHallmarkWeight(int date) {
        if (hallmark == null || ! hallmark.isValid() || hallmark.getDate() != date) {
            return 0;
        }
        return hallmark.getWeight();
    }

}
