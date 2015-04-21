package nhz.http;

public enum APITag {

    ACCOUNTS("Accounts"), ALIASES("Aliases"), AE("Asset Exchange"), CREATE_TRANSACTION("Create Transaction"),
    BLOCKS("Blocks"), DGS("Digital Goods Store"), FORGING("Forging"), INFO("Server Info"), MESSAGES("Messages"),
    TRANSACTIONS("Transactions"), TOKENS("Tokens"), VS("Voting System"), XBRIDGE("XBridge");

    private final String displayName;

    private APITag(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
