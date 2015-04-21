package nhz;

import nhz.crypto.Crypto;
import nhz.util.Convert;
import nhz.util.Logger;
import org.json.simple.JSONObject;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class TransactionImpl implements Transaction {

    static final class BuilderImpl implements Builder {

        private final short deadline;
        private final byte[] senderPublicKey;
        private final long amountNQT;
        private final long feeNQT;
        private final TransactionType type;
        private final byte version;
        private final int timestamp;
        private final Attachment.AbstractAttachment attachment;

        private Long recipientId;
        private String referencedTransactionFullHash;
        private byte[] signature;
        private Appendix.Message message;
        private Appendix.EncryptedMessage encryptedMessage;
        private Appendix.EncryptToSelfMessage encryptToSelfMessage;
        private Appendix.PublicKeyAnnouncement publicKeyAnnouncement;
        private Long blockId;
        private int height = Integer.MAX_VALUE;
        private Long id;
        private Long senderId;
        private int blockTimestamp = -1;
        private String fullHash;
        private int ecBlockHeight;
        private Long ecBlockId;

        BuilderImpl(byte version, byte[] senderPublicKey, long amountNQT, long feeNQT, int timestamp, short deadline,
                    Attachment.AbstractAttachment attachment) {
            this.version = version;
            this.timestamp = timestamp;
            this.deadline = deadline;
            this.senderPublicKey = senderPublicKey;
            this.amountNQT = amountNQT;
            this.feeNQT = feeNQT;
            this.attachment = attachment;
            this.type = attachment.getTransactionType();
        }

        @Override
        public TransactionImpl build() throws NhzException.NotValidException {
            return new TransactionImpl(this);
        }

        @Override
        public BuilderImpl recipientId(Long recipientId) {
            this.recipientId = recipientId;
            return this;
        }

        @Override
        public BuilderImpl referencedTransactionFullHash(String referencedTransactionFullHash) {
            this.referencedTransactionFullHash = referencedTransactionFullHash;
            return this;
        }

        BuilderImpl referencedTransactionFullHash(byte[] referencedTransactionFullHash) {
            if (referencedTransactionFullHash != null) {
                this.referencedTransactionFullHash = Convert.toHexString(referencedTransactionFullHash);
            }
            return this;
        }

        @Override
        public BuilderImpl message(Appendix.Message message) {
            this.message = message;
            return this;
        }

        @Override
        public BuilderImpl encryptedMessage(Appendix.EncryptedMessage encryptedMessage) {
            this.encryptedMessage = encryptedMessage;
            return this;
        }

        @Override
        public BuilderImpl encryptToSelfMessage(Appendix.EncryptToSelfMessage encryptToSelfMessage) {
            this.encryptToSelfMessage = encryptToSelfMessage;
            return this;
        }

        @Override
        public BuilderImpl publicKeyAnnouncement(Appendix.PublicKeyAnnouncement publicKeyAnnouncement) {
            this.publicKeyAnnouncement = publicKeyAnnouncement;
            return this;
        }

        BuilderImpl id(Long id) {
            this.id = id;
            return this;
        }

        BuilderImpl signature(byte[] signature) {
            this.signature = signature;
            return this;
        }

        BuilderImpl blockId(Long blockId) {
            this.blockId = blockId;
            return this;
        }

        BuilderImpl height(int height) {
            this.height = height;
            return this;
        }

        BuilderImpl senderId(Long senderId) {
            this.senderId = senderId;
            return this;
        }

        BuilderImpl fullHash(String fullHash) {
            this.fullHash = fullHash;
            return this;
        }

        BuilderImpl fullHash(byte[] fullHash) {
            if (fullHash != null) {
                this.fullHash = Convert.toHexString(fullHash);
            }
            return this;
        }

        BuilderImpl blockTimestamp(int blockTimestamp) {
            this.blockTimestamp = blockTimestamp;
            return this;
        }

        BuilderImpl ecBlockHeight(int height) {
            this.ecBlockHeight = height;
            return this;
        }

        BuilderImpl ecBlockId(Long blockId) {
            this.ecBlockId = blockId;
            return this;
        }

    }

    private final short deadline;
    private final byte[] senderPublicKey;
    private final Long recipientId;
    private final long amountNQT;
    private final long feeNQT;
    private final String referencedTransactionFullHash;
    private final TransactionType type;
    private final int ecBlockHeight;
    private final Long ecBlockId;
    private final byte version;
    private final int timestamp;
    private final Attachment.AbstractAttachment attachment;
    private final Appendix.Message message;
    private final Appendix.EncryptedMessage encryptedMessage;
    private final Appendix.EncryptToSelfMessage encryptToSelfMessage;
    private final Appendix.PublicKeyAnnouncement publicKeyAnnouncement;

    private final List<? extends Appendix.AbstractAppendix> appendages;
    private final int appendagesSize;

    private volatile int height = Integer.MAX_VALUE;
    private volatile Long blockId;
    private volatile Block block;
    private volatile byte[] signature;
    private volatile int blockTimestamp = -1;
    private volatile Long id;
    private volatile String stringId;
    private volatile Long senderId;
    private volatile String fullHash;

    private TransactionImpl(BuilderImpl builder) throws NhzException.NotValidException {

        this.timestamp = builder.timestamp;
        this.deadline = builder.deadline;
        this.senderPublicKey = builder.senderPublicKey;
        this.recipientId = builder.recipientId;
        this.amountNQT = builder.amountNQT;
        this.feeNQT = builder.feeNQT;
        this.referencedTransactionFullHash = builder.referencedTransactionFullHash;
        this.signature = builder.signature;
        this.type = builder.type;
        this.version = builder.version;
        this.blockId = builder.blockId;
        this.height = builder.height;
        this.id = builder.id;
        this.senderId = builder.senderId;
        this.blockTimestamp = builder.blockTimestamp;
        this.fullHash = builder.fullHash;
		this.ecBlockHeight = builder.ecBlockHeight;
        this.ecBlockId = builder.ecBlockId;

        List<Appendix.AbstractAppendix> list = new ArrayList<>();
        if ((this.attachment = builder.attachment) != null) {
            list.add(this.attachment);
        }
        if ((this.message  = builder.message) != null) {
            list.add(this.message);
        }
        if ((this.encryptedMessage = builder.encryptedMessage) != null) {
            list.add(this.encryptedMessage);
        }
        if ((this.publicKeyAnnouncement = builder.publicKeyAnnouncement) != null) {
            list.add(this.publicKeyAnnouncement);
        }
        if ((this.encryptToSelfMessage = builder.encryptToSelfMessage) != null) {
            list.add(this.encryptToSelfMessage);
        }
        this.appendages = Collections.unmodifiableList(list);
        int appendagesSize = 0;
        for (Appendix appendage : appendages) {
            appendagesSize += appendage.getSize();
        }
        this.appendagesSize = appendagesSize;

        if ((timestamp == 0 && Arrays.equals(senderPublicKey, Genesis.CREATOR_PUBLIC_KEY))
                ? (deadline != 0 || feeNQT != 0)
                : (deadline < 1 || feeNQT < Constants.ONE_NHZ)
                || feeNQT > Constants.MAX_BALANCE_NQT
                || amountNQT < 0
                || amountNQT > Constants.MAX_BALANCE_NQT
                || type == null) {
            throw new NhzException.NotValidException("Invalid transaction parameters:\n type: " + type + ", timestamp: " + timestamp
                    + ", deadline: " + deadline + ", fee: " + feeNQT + ", amount: " + amountNQT);
        }

        if (attachment == null || type != attachment.getTransactionType()) {
            throw new NhzException.NotValidException("Invalid attachment " + attachment + " for transaction of type " + type);
        }

        if (! type.hasRecipient()) {
            if (recipientId != null && !recipientId.equals(Genesis.CREATOR_ID) || getAmountNQT() != 0) {
                throw new NhzException.NotValidException("Transactions of this type must have recipient == Genesis, amount == 0");
            }
        }

        for (Appendix.AbstractAppendix appendage : appendages) {
            if (! appendage.verifyVersion(this.version)) {
                throw new NhzException.NotValidException("Invalid attachment version " + appendage.getVersion()
                        + " for transaction version " + this.version);
            }
        }

    }

    @Override
    public short getDeadline() {
        return deadline;
    }

    @Override
    public byte[] getSenderPublicKey() {
        return senderPublicKey;
    }

    @Override
    public Long getRecipientId() {
        return recipientId;
    }

    @Override
    public long getAmountNQT() {
        return amountNQT;
    }

    @Override
    public long getFeeNQT() {
        return feeNQT;
    }

    @Override
    public String getReferencedTransactionFullHash() {
        return referencedTransactionFullHash;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public byte[] getSignature() {
        return signature;
    }

    @Override
    public TransactionType getType() {
        return type;
    }

    @Override
    public byte getVersion() {
        return version;
    }

    @Override
    public Long getBlockId() {
        return blockId;
    }

    @Override
    public Block getBlock() {
        if (block == null && blockId != null) {
            block = BlockDb.findBlock(blockId);
        }
        return block;
    }

    void setBlock(Block block) {
        this.block = block;
        this.blockId = block.getId();
        this.height = block.getHeight();
        this.blockTimestamp = block.getTimestamp();
    }

    private void unsetBlock() {
        this.block = null;
        this.blockId = null;
        this.blockTimestamp = -1;
        // must keep the height set, as transactions already having been included in a popped-off block before
        // get priority when sorted for inclusion in a new block
    }

    @Override
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public int getBlockTimestamp() {
        return blockTimestamp;
    }

    @Override
    public int getExpiration() {
        return timestamp + deadline * 60;
    }

    @Override
    public Attachment getAttachment() {
        return attachment;
    }

    @Override
    public List<? extends Appendix> getAppendages() {
        return appendages;
    }

    @Override
    public Long getId() {
        if (id == null) {
            if (signature == null) {
                throw new IllegalStateException("Transaction is not signed yet");
            }
            byte[] hash;
            if (useNQT()) {
                byte[] data = zeroSignature(getBytes());
                byte[] signatureHash = Crypto.sha256().digest(signature);
                MessageDigest digest = Crypto.sha256();
                digest.update(data);
                hash = digest.digest(signatureHash);
            } else {
                hash = Crypto.sha256().digest(getBytes());
            }
            BigInteger bigInteger = new BigInteger(1, new byte[] {hash[7], hash[6], hash[5], hash[4], hash[3], hash[2], hash[1], hash[0]});
            id = bigInteger.longValue();
            stringId = bigInteger.toString();
            fullHash = Convert.toHexString(hash);
        }
        return id;
    }

    @Override
    public String getStringId() {
        if (stringId == null) {
            getId();
            if (stringId == null) {
                stringId = Convert.toUnsignedLong(id);
            }
        }
        return stringId;
    }

    @Override
    public String getFullHash() {
        if (fullHash == null) {
            getId();
        }
        return fullHash;
    }

    @Override
    public Long getSenderId() {
        if (senderId == null) {
            senderId = Account.getId(senderPublicKey);
        }
        return senderId;
    }

    @Override
    public Appendix.Message getMessage() {
        return message;
    }

    @Override
    public Appendix.EncryptedMessage getEncryptedMessage() {
        return encryptedMessage;
    }

    @Override
    public Appendix.EncryptToSelfMessage getEncryptToSelfMessage() {
        return encryptToSelfMessage;
    }

    Appendix.PublicKeyAnnouncement getPublicKeyAnnouncement() {
        return publicKeyAnnouncement;
    }

    public int compareTo(Transaction o) {

        if (height < o.getHeight()) {
            return -1;
        }
        if (height > o.getHeight()) {
            return 1;
        }
        // equivalent to: fee * 1048576L / getSize() > o.fee * 1048576L / o.getSize()
        if (Convert.safeMultiply(feeNQT, ((TransactionImpl)o).getSize()) > Convert.safeMultiply(o.getFeeNQT(), getSize())) {
            return -1;
        }
        if (Convert.safeMultiply(feeNQT, ((TransactionImpl)o).getSize()) < Convert.safeMultiply(o.getFeeNQT(), getSize())) {
            return 1;
        }
        if (timestamp < o.getTimestamp()) {
            return -1;
        }
        if (timestamp > o.getTimestamp()) {
            return 1;
        }
        if (getId() < o.getId()) {
            return -1;
        }
        if (getId() > o.getId()) {
            return 1;
        }
        return 0;

    }

    @Override
    public byte[] getBytes() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(getSize());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(type.getType());
            buffer.put((byte) ((version << 4) | type.getSubtype()));
            buffer.putInt(timestamp);
            buffer.putShort(deadline);
            buffer.put(senderPublicKey);
            buffer.putLong(type.hasRecipient() ? Convert.nullToZero(recipientId) : Genesis.CREATOR_ID);
            if (useNQT()) {
                buffer.putLong(amountNQT);
                buffer.putLong(feeNQT);
                if (referencedTransactionFullHash != null) {
                    buffer.put(Convert.parseHexString(referencedTransactionFullHash));
                } else {
                    buffer.put(new byte[32]);
                }
            } else {
                buffer.putInt((int) (amountNQT / Constants.ONE_NHZ));
                buffer.putInt((int) (feeNQT / Constants.ONE_NHZ));
                if (referencedTransactionFullHash != null) {
                    buffer.putLong(Convert.fullHashToId(Convert.parseHexString(referencedTransactionFullHash)));
                } else {
                    buffer.putLong(0L);
                }
            }
            buffer.put(signature != null ? signature : new byte[64]);
            if (version > 0) {
                buffer.putInt(getFlags());
                buffer.putInt(ecBlockHeight);
                buffer.putLong(ecBlockId);
            }
            for (Appendix appendage : appendages) {
                appendage.putBytes(buffer);
            }
            return buffer.array();
        } catch (RuntimeException e) {
            Logger.logDebugMessage("Failed to get transaction bytes for transaction: " + getJSONObject().toJSONString());
            throw e;
        }
    }

    static TransactionImpl parseTransaction(byte[] bytes) throws NhzException.ValidationException {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            byte type = buffer.get();
            byte subtype = buffer.get();
            byte version = (byte) ((subtype & 0xF0) >> 4);
            subtype = (byte) (subtype & 0x0F);
            int timestamp = buffer.getInt();
            short deadline = buffer.getShort();
            byte[] senderPublicKey = new byte[32];
            buffer.get(senderPublicKey);
            Long recipientId = Convert.zeroToNull(buffer.getLong());
            long amountNQT = buffer.getLong();
            long feeNQT = buffer.getLong();
            String referencedTransactionFullHash = null;
            byte[] referencedTransactionFullHashBytes = new byte[32];
            buffer.get(referencedTransactionFullHashBytes);
            if (Convert.emptyToNull(referencedTransactionFullHashBytes) != null) {
                referencedTransactionFullHash = Convert.toHexString(referencedTransactionFullHashBytes);
            }
            byte[] signature = new byte[64];
            buffer.get(signature);
            signature = Convert.emptyToNull(signature);
            int flags = 0;
            int ecBlockHeight = 0;
            Long ecBlockId = null;
            if (version > 0) {
                flags = buffer.getInt();
                ecBlockHeight = buffer.getInt();
                ecBlockId = buffer.getLong();
            }
            TransactionType transactionType = TransactionType.findTransactionType(type, subtype);
            TransactionImpl.BuilderImpl builder = new TransactionImpl.BuilderImpl(version, senderPublicKey, amountNQT, feeNQT,
                    timestamp, deadline, transactionType.parseAttachment(buffer, version))
                    .referencedTransactionFullHash(referencedTransactionFullHash)
                    .signature(signature)
                    .ecBlockHeight(ecBlockHeight)
                    .ecBlockId(ecBlockId);
            if (transactionType.hasRecipient() || (recipientId != null && recipientId.equals(Genesis.CREATOR_ID))) {
                builder.recipientId(recipientId);
            }
            int position = 1;
            if ((flags & position) != 0 || (version == 0 && transactionType == TransactionType.Messaging.ARBITRARY_MESSAGE)) {
                builder.message(new Appendix.Message(buffer, version));
            }
            position <<= 1;
            if ((flags & position) != 0) {
                builder.encryptedMessage(new Appendix.EncryptedMessage(buffer, version));
            }
            position <<= 1;
            if ((flags & position) != 0) {
                builder.publicKeyAnnouncement(new Appendix.PublicKeyAnnouncement(buffer, version));
            }
            position <<= 1;
            if ((flags & position) != 0) {
                builder.encryptToSelfMessage(new Appendix.EncryptToSelfMessage(buffer, version));
            }
            return builder.build();
        } catch (NhzException.NotValidException|RuntimeException e) {
            Logger.logDebugMessage("Failed to parse transaction bytes: " + Convert.toHexString(bytes));
            throw e;
        }
    }

    @Override
    public byte[] getUnsignedBytes() {
        return zeroSignature(getBytes());
    }

    /*
    @Override
    public Collection<TransactionType> getPhasingTransactionTypes() {
        return getType().getPhasingTransactionTypes();
    }

    @Override
    public Collection<TransactionType> getPhasedTransactionTypes() {
        return getType().getPhasedTransactionTypes();
    }
    */

    @Override
    public JSONObject getJSONObject() {
        JSONObject json = new JSONObject();
        json.put("type", type.getType());
        json.put("subtype", type.getSubtype());
        json.put("timestamp", timestamp);
        json.put("deadline", deadline);
        json.put("senderPublicKey", Convert.toHexString(senderPublicKey));
        if (type.hasRecipient() || (recipientId != null && recipientId.equals(Genesis.CREATOR_ID))) {
            json.put("recipient", Convert.toUnsignedLong(recipientId));
        }
        json.put("amountNQT", amountNQT);
        json.put("feeNQT", feeNQT);
        if (referencedTransactionFullHash != null) {
            json.put("referencedTransactionFullHash", referencedTransactionFullHash);
        }
        json.put("ecBlockHeight", ecBlockHeight);
        json.put("ecBlockId", Convert.toUnsignedLong(ecBlockId));
        json.put("signature", Convert.toHexString(signature));
        JSONObject attachmentJSON = new JSONObject();
        for (Appendix appendage : appendages) {
            attachmentJSON.putAll(appendage.getJSONObject());
        }
        if (! attachmentJSON.isEmpty()) {
            json.put("attachment", attachmentJSON);
        }
        json.put("version", version);
        return json;
    }

    static TransactionImpl parseTransaction(JSONObject transactionData) throws NhzException.NotValidException {
        try {
            byte type = ((Long) transactionData.get("type")).byteValue();
            byte subtype = ((Long) transactionData.get("subtype")).byteValue();
            int timestamp = ((Long) transactionData.get("timestamp")).intValue();
            short deadline = ((Long) transactionData.get("deadline")).shortValue();
            byte[] senderPublicKey = Convert.parseHexString((String) transactionData.get("senderPublicKey"));
            long amountNQT = Convert.parseLong(transactionData.get("amountNQT"));
            long feeNQT = Convert.parseLong(transactionData.get("feeNQT"));
            String referencedTransactionFullHash = (String) transactionData.get("referencedTransactionFullHash");
            byte[] signature = Convert.parseHexString((String) transactionData.get("signature"));
            Long versionValue = (Long) transactionData.get("version");
            byte version = versionValue == null ? 0 : versionValue.byteValue();
            JSONObject attachmentData = (JSONObject) transactionData.get("attachment");

            TransactionType transactionType = TransactionType.findTransactionType(type, subtype);
            if (transactionType == null) {
                throw new NhzException.NotValidException("Invalid transaction type: " + type + ", " + subtype);
            }
            TransactionImpl.BuilderImpl builder = new TransactionImpl.BuilderImpl(version, senderPublicKey,
                    amountNQT, feeNQT, timestamp, deadline,
                    transactionType.parseAttachment(attachmentData))
                    .referencedTransactionFullHash(referencedTransactionFullHash)
                    .signature(signature);
            Long recipientId = Convert.parseUnsignedLong((String) transactionData.get("recipient"));            
            if (transactionType.hasRecipient() || (recipientId != null && recipientId.equals(Genesis.CREATOR_ID))) {
                builder.recipientId(recipientId);
            }
            if (attachmentData != null) {
                builder.message(Appendix.Message.parse(attachmentData));
                builder.encryptedMessage(Appendix.EncryptedMessage.parse(attachmentData));
                builder.publicKeyAnnouncement((Appendix.PublicKeyAnnouncement.parse(attachmentData)));
                builder.encryptToSelfMessage(Appendix.EncryptToSelfMessage.parse(attachmentData));
            }
            if (version > 0) {
                builder.ecBlockHeight(((Long) transactionData.get("ecBlockHeight")).intValue());
                builder.ecBlockId(Convert.parseUnsignedLong((String) transactionData.get("ecBlockId")));
            }
            return builder.build();
        } catch (NhzException.NotValidException|RuntimeException e) {
            Logger.logDebugMessage("Failed to parse transaction: " + transactionData.toJSONString());
            throw e;
        }
    }


    @Override
    public int getECBlockHeight() {
        return ecBlockHeight;
    }

    @Override
    public Long getECBlockId() {
        return ecBlockId;
    }

    @Override
    public void sign(String secretPhrase) {
        if (signature != null) {
            throw new IllegalStateException("Transaction already signed");
        }
        signature = Crypto.sign(getBytes(), secretPhrase);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TransactionImpl && this.getId().equals(((Transaction)o).getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public boolean verifySignature() {
        Account account = Account.getAccount(getSenderId());
        if (account == null) {
            return false;
        }
        if (signature == null) {
            return false;
        }
        byte[] data = zeroSignature(getBytes());
        return Crypto.verify(signature, data, senderPublicKey, useNQT()) && account.setOrVerify(senderPublicKey, this.getHeight());
    }

    int getSize() {
        return signatureOffset() + 64  + (version > 0 ? 4 + 4 + 8 : 0) + appendagesSize;
    }

    private int signatureOffset() {
        return 1 + 1 + 4 + 2 + 32 + 8 + (useNQT() ? 8 + 8 + 32 : 4 + 4 + 8);
    }

    private boolean useNQT() {
        return this.height > Constants.NQT_BLOCK
                && (this.height < Integer.MAX_VALUE
                || Nhz.getBlockchain().getHeight() >= Constants.NQT_BLOCK);
    }

    private byte[] zeroSignature(byte[] data) {
        int start = signatureOffset();
        for (int i = start; i < start + 64; i++) {
            data[i] = 0;
        }
        return data;
    }

    private int getFlags() {
        int flags = 0;
        int position = 1;
        if (message != null) {
            flags |= position;
        }
        position <<= 1;
        if (encryptedMessage != null) {
            flags |= position;
        }
        position <<= 1;
        if (publicKeyAnnouncement != null) {
            flags |= position;
        }
        position <<= 1;
        if (encryptToSelfMessage != null) {
            flags |= position;
        }
        return flags;
    }

    @Override
    public void validate() throws NhzException.ValidationException {
        if (Nhz.getBlockchain().getHeight() >= Constants.PUBLIC_KEY_ANNOUNCEMENT_BLOCK && type.hasRecipient() && recipientId != null) {
            Account recipientAccount = Account.getAccount(recipientId);
            if ((recipientAccount == null || recipientAccount.getPublicKey() == null) && publicKeyAnnouncement == null) {
                throw new NhzException.NotCurrentlyValidException("Recipient account does not have a public key, must attach a public key announcement");
            }
        }
        for (Appendix.AbstractAppendix appendage : appendages) {
            appendage.validate(this);
        }
    }

    // returns false iff double spending
    boolean applyUnconfirmed() {
        Account senderAccount = Account.getAccount(getSenderId());
        if (senderAccount == null) {
            return false;
        }
        synchronized(senderAccount) {
            return type.applyUnconfirmed(this, senderAccount);
        }
    }

    void apply() {
        Account senderAccount = Account.getAccount(getSenderId());
        senderAccount.apply(senderPublicKey, this.getHeight());
        Account recipientAccount = Account.getAccount(recipientId);
        if (recipientAccount == null && recipientId != null) {
            recipientAccount = Account.addOrGetAccount(recipientId);
        }
        for (Appendix.AbstractAppendix appendage : appendages) {
            appendage.apply(this, senderAccount, recipientAccount);
        }
    }

    void undoUnconfirmed() {
        Account senderAccount = Account.getAccount(getSenderId());
        type.undoUnconfirmed(this, senderAccount);
    }

    // NOTE: when undo is called, lastBlock has already been set to the previous block
    void undo() throws TransactionType.UndoNotSupportedException {
        Account senderAccount = Account.getAccount(senderId);
        Account recipientAccount = Account.getAccount(recipientId);
        for (Appendix.AbstractAppendix appendage : appendages) {
            appendage.undo(this, senderAccount, recipientAccount);
        }
        senderAccount.undo(this.getHeight());
        unsetBlock();
    }

    boolean isDuplicate(Map<TransactionType, Set<String>> duplicates) {
        return type.isDuplicate(this, duplicates);
    }

}
