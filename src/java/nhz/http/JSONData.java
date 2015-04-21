package nhz.http;

import nhz.Account;
import nhz.Alias;
import nhz.Appendix;
import nhz.Asset;
import nhz.Block;
import nhz.DigitalGoodsStore;
import nhz.Nhz;
import nhz.Order;
import nhz.Poll;
import nhz.Token;
import nhz.Trade;
import nhz.Transaction;
import nhz.TransactionType;
import nhz.crypto.Crypto;
import nhz.crypto.EncryptedData;
import nhz.peer.Hallmark;
import nhz.peer.Peer;
import nhz.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collections;

final class JSONData {

    static JSONObject alias(Alias alias) {
        JSONObject json = new JSONObject();
        putAccount(json, "account", alias.getAccountId());
        json.put("aliasName", alias.getAliasName());
        json.put("aliasURI", alias.getAliasURI());
        json.put("timestamp", alias.getTimestamp());
        json.put("alias", Convert.toUnsignedLong(alias.getId()));
        Alias.Offer offer = Alias.getOffer(alias.getAliasName());
        if (offer != null) {
            json.put("priceNQT", String.valueOf(offer.getPriceNQT()));
            if (offer.getBuyerId() != null) {
                json.put("buyer", Convert.toUnsignedLong(offer.getBuyerId()));
            }
        }
        return json;
    }

    static JSONObject accountBalance(Account account) {
        JSONObject json = new JSONObject();
        if (account == null) {
            json.put("balanceNQT", "0");
            json.put("unconfirmedBalanceNQT", "0");
            json.put("effectiveBalanceNHZ", "0");
            json.put("forgedBalanceNQT", "0");
            json.put("guaranteedBalanceNQT", "0");
        } else {
            synchronized (account) { // to make sure balance and unconfirmedBalance are consistent
                json.put("balanceNQT", String.valueOf(account.getBalanceNQT()));
                json.put("unconfirmedBalanceNQT", String.valueOf(account.getUnconfirmedBalanceNQT()));
                json.put("effectiveBalanceNHZ", account.getEffectiveBalanceNHZ());
                json.put("forgedBalanceNQT", String.valueOf(account.getForgedBalanceNQT()));
                json.put("guaranteedBalanceNQT", String.valueOf(account.getGuaranteedBalanceNQT(40)));
            }
        }
        return json;
    }

    static JSONObject asset(Asset asset) {
        JSONObject json = new JSONObject();
        putAccount(json, "account", asset.getAccountId());
        json.put("name", asset.getName());
        json.put("description", asset.getDescription());
        json.put("decimals", asset.getDecimals());
        json.put("quantityQNT", String.valueOf(asset.getQuantityQNT()));
        json.put("asset", Convert.toUnsignedLong(asset.getId()));
        json.put("numberOfTrades", Trade.getTrades(asset.getId()).size());
        return json;
    }

    static JSONObject askOrder(Order.Ask order) {
        JSONObject json = order(order);
        json.put("type", "ask");
        return json;
    }

    static JSONObject bidOrder(Order.Bid order) {
        JSONObject json = order(order);
        json.put("type", "bid");
        return json;
    }

    static JSONObject order(Order order) {
        JSONObject json = new JSONObject();
        json.put("order", Convert.toUnsignedLong(order.getId()));
        json.put("asset", Convert.toUnsignedLong(order.getAssetId()));
        putAccount(json, "account", order.getAccount().getId());
        json.put("quantityQNT", String.valueOf(order.getQuantityQNT()));
        json.put("priceNQT", String.valueOf(order.getPriceNQT()));
        json.put("height", order.getHeight());
        return json;
    }

    static JSONObject block(Block block, boolean includeTransactions) {
        JSONObject json = new JSONObject();
        json.put("block", block.getStringId());
        json.put("height", block.getHeight());
        putAccount(json, "generator", block.getGeneratorId());
        json.put("generatorPublicKey", Convert.toHexString(block.getGeneratorPublicKey()));
        json.put("timestamp", block.getTimestamp());
        json.put("numberOfTransactions", block.getTransactionIds().size());
        json.put("totalAmountNQT", String.valueOf(block.getTotalAmountNQT()));
        json.put("totalFeeNQT", String.valueOf(block.getTotalFeeNQT()));
        json.put("payloadLength", block.getPayloadLength());
        json.put("version", block.getVersion());
        json.put("baseTarget", Convert.toUnsignedLong(block.getBaseTarget()));
        if (block.getPreviousBlockId() != null) {
            json.put("previousBlock", Convert.toUnsignedLong(block.getPreviousBlockId()));
        }
        if (block.getNextBlockId() != null) {
            json.put("nextBlock", Convert.toUnsignedLong(block.getNextBlockId()));
        }
        json.put("payloadHash", Convert.toHexString(block.getPayloadHash()));
        json.put("generationSignature", Convert.toHexString(block.getGenerationSignature()));
        if (block.getVersion() > 1) {
            json.put("previousBlockHash", Convert.toHexString(block.getPreviousBlockHash()));
        }
        json.put("blockSignature", Convert.toHexString(block.getBlockSignature()));
        JSONArray transactions = new JSONArray();
        for (Transaction transaction : block.getTransactions()) {
            transactions.add(includeTransactions ? transaction(transaction) : Convert.toUnsignedLong(transaction.getId()));
        }
        json.put("transactions", transactions);
        return json;
    }

    static JSONObject encryptedData(EncryptedData encryptedData) {
        JSONObject json = new JSONObject();
        json.put("data", Convert.toHexString(encryptedData.getData()));
        json.put("nonce", Convert.toHexString(encryptedData.getNonce()));
        return json;
    }

    static JSONObject goods(DigitalGoodsStore.Goods goods) {
        JSONObject json = new JSONObject();
        json.put("goods", Convert.toUnsignedLong(goods.getId()));
        json.put("name", goods.getName());
        json.put("description", goods.getDescription());
        json.put("quantity", goods.getQuantity());
        json.put("priceNQT", String.valueOf(goods.getPriceNQT()));
        putAccount(json, "seller", goods.getSellerId());
        json.put("tags", goods.getTags());
        json.put("delisted", goods.isDelisted());
        return json;
    }

    static JSONObject hallmark(Hallmark hallmark) {
        JSONObject json = new JSONObject();
        putAccount(json, "account", Account.getId(hallmark.getPublicKey()));
        json.put("host", hallmark.getHost());
        json.put("weight", hallmark.getWeight());
        String dateString = Hallmark.formatDate(hallmark.getDate());
        json.put("date", dateString);
        json.put("valid", hallmark.isValid());
        return json;
    }

    static JSONObject token(Token token) {
        JSONObject json = new JSONObject();
        putAccount(json, "account", Account.getId(token.getPublicKey()));
        json.put("timestamp", token.getTimestamp());
        json.put("valid", token.isValid());
        return json;
    }

    static JSONObject peer(Peer peer) {
        JSONObject json = new JSONObject();
        json.put("state", peer.getState().ordinal());
        json.put("announcedAddress", peer.getAnnouncedAddress());
        json.put("shareAddress", peer.shareAddress());
        if (peer.getHallmark() != null) {
            json.put("hallmark", peer.getHallmark().getHallmarkString());
        }
        json.put("weight", peer.getWeight());
        json.put("downloadedVolume", peer.getDownloadedVolume());
        json.put("uploadedVolume", peer.getUploadedVolume());
        json.put("application", peer.getApplication());
        json.put("version", peer.getVersion());
        json.put("platform", peer.getPlatform());
        json.put("blacklisted", peer.isBlacklisted());
        json.put("lastUpdated", peer.getLastUpdated());
        return json;
    }

    static JSONObject poll(Poll poll) {
        JSONObject json = new JSONObject();
        json.put("name", poll.getName());
        json.put("description", poll.getDescription());
        JSONArray options = new JSONArray();
        Collections.addAll(options, poll.getOptions());
        json.put("options", options);
        json.put("minNumberOfOptions", poll.getMinNumberOfOptions());
        json.put("maxNumberOfOptions", poll.getMaxNumberOfOptions());
        json.put("optionsAreBinary", poll.isOptionsAreBinary());
        JSONArray voters = new JSONArray();
        for (Long voterId : poll.getVoters().keySet()) {
            voters.add(Convert.toUnsignedLong(voterId));
        }
        json.put("voters", voters);
        return json;
    }

    static JSONObject purchase(DigitalGoodsStore.Purchase purchase) {
        JSONObject json = new JSONObject();
        json.put("purchase", Convert.toUnsignedLong(purchase.getId()));
        json.put("goods", Convert.toUnsignedLong(purchase.getGoodsId()));
        json.put("name", purchase.getName());
        putAccount(json, "seller", purchase.getSellerId());
        json.put("priceNQT", String.valueOf(purchase.getPriceNQT()));
        json.put("quantity", purchase.getQuantity());
        putAccount(json, "buyer", purchase.getBuyerId());
        json.put("timestamp", purchase.getTimestamp());
        json.put("deliveryDeadlineTimestamp", purchase.getDeliveryDeadlineTimestamp());
        if (purchase.getNote() != null) {
            json.put("note", encryptedData(purchase.getNote()));
        }
        json.put("pending", purchase.isPending());
        if (purchase.getEncryptedGoods() != null) {
            json.put("goodsData", encryptedData(purchase.getEncryptedGoods()));
            json.put("goodsIsText", purchase.goodsIsText());
        }
        if (purchase.getFeedbackNotes() != null) {
            JSONArray feedbacks = new JSONArray();
            for (EncryptedData encryptedData : purchase.getFeedbackNotes()) {
                feedbacks.add(encryptedData(encryptedData));
            }
            json.put("feedbackNotes", feedbacks);
        }
        if (purchase.getPublicFeedback() != null) {
            JSONArray publicFeedbacks = new JSONArray();
            for (String publicFeedback : purchase.getPublicFeedback()) {
                publicFeedbacks.add(publicFeedback);
            }
            json.put("publicFeedbacks", publicFeedbacks);
        }
        if (purchase.getRefundNote() != null) {
            json.put("refundNote", encryptedData(purchase.getRefundNote()));
        }
        if (purchase.getDiscountNQT() > 0) {
            json.put("discountNQT", String.valueOf(purchase.getDiscountNQT()));
        }
        if (purchase.getRefundNQT() > 0) {
            json.put("refundNQT", String.valueOf(purchase.getRefundNQT()));
        }
        return json;
    }

    static JSONObject trade(Trade trade) {
        JSONObject json = new JSONObject();
        json.put("timestamp", trade.getTimestamp());
        json.put("quantityQNT", String.valueOf(trade.getQuantityQNT()));
        json.put("priceNQT", String.valueOf(trade.getPriceNQT()));
        json.put("asset", Convert.toUnsignedLong(trade.getAssetId()));
        json.put("askOrder", Convert.toUnsignedLong(trade.getAskOrderId()));
        json.put("bidOrder", Convert.toUnsignedLong(trade.getBidOrderId()));
        json.put("block", Convert.toUnsignedLong(trade.getBlockId()));
        return json;
    }

    static JSONObject unconfirmedTransaction(Transaction transaction) {
        JSONObject json = new JSONObject();
        json.put("type", transaction.getType().getType());
        json.put("subtype", transaction.getType().getSubtype());
        json.put("timestamp", transaction.getTimestamp());
        json.put("deadline", transaction.getDeadline());
        json.put("senderPublicKey", Convert.toHexString(transaction.getSenderPublicKey()));
        if (transaction.getRecipientId() != null) {
            putAccount(json, "recipient", transaction.getRecipientId());
        }
        json.put("amountNQT", String.valueOf(transaction.getAmountNQT()));
        json.put("feeNQT", String.valueOf(transaction.getFeeNQT()));
        if (transaction.getReferencedTransactionFullHash() != null) {
            json.put("referencedTransactionFullHash", transaction.getReferencedTransactionFullHash());
        }
        byte[] signature = Convert.emptyToNull(transaction.getSignature());
        if (signature != null) {
            json.put("signature", Convert.toHexString(signature));
            json.put("signatureHash", Convert.toHexString(Crypto.sha256().digest(signature)));
            json.put("fullHash", transaction.getFullHash());
            json.put("transaction", transaction.getStringId());
        }
        JSONObject attachmentJSON = new JSONObject();
        for (Appendix appendage : transaction.getAppendages()) {
            attachmentJSON.putAll(appendage.getJSONObject());
        }
        if (! attachmentJSON.isEmpty()) {
            modifyAttachmentJSON(attachmentJSON);
            if (transaction.getType() == TransactionType.ColoredCoins.ASSET_TRANSFER) {
                //TODO: remove some time after DGS block
                String comment = (String)attachmentJSON.get("comment");
                String message = (String)attachmentJSON.get("message");
                if (comment == null && message != null) {
                    attachmentJSON.put("comment", message);
                }
            }
            json.put("attachment", attachmentJSON);
        }
        putAccount(json, "sender", transaction.getSenderId());
        json.put("height", transaction.getHeight());
        json.put("version", transaction.getVersion());
        if (transaction.getVersion() > 0) {
            json.put("ecBlockId", Convert.toUnsignedLong(transaction.getECBlockId()));
            json.put("ecBlockHeight", transaction.getECBlockHeight());
        }

        return json;
    }

    static JSONObject transaction(Transaction transaction) {
        JSONObject json = unconfirmedTransaction(transaction);
        json.put("block", Convert.toUnsignedLong(transaction.getBlockId()));
        json.put("confirmations", Nhz.getBlockchain().getLastBlock().getHeight() - transaction.getHeight());
        json.put("blockTimestamp", transaction.getBlockTimestamp());
        return json;
    }

    // ugly, hopefully temporary
    private static void modifyAttachmentJSON(JSONObject json) {
        Long quantityQNT = (Long) json.remove("quantityQNT");
        if (quantityQNT != null) {
            json.put("quantityQNT", String.valueOf(quantityQNT));
        }
        Long priceNQT = (Long) json.remove("priceNQT");
        if (priceNQT != null) {
            json.put("priceNQT", String.valueOf(priceNQT));
        }
        Long discountNQT = (Long) json.remove("discountNQT");
        if (discountNQT != null) {
            json.put("discountNQT", String.valueOf(discountNQT));
        }
        Long refundNQT = (Long) json.remove("refundNQT");
        if (refundNQT != null) {
            json.put("refundNQT", String.valueOf(refundNQT));
        }
    }

    static void putAccount(JSONObject json, String name, Long accountId) {
        json.put(name, Convert.toUnsignedLong(accountId));
        json.put(name + "RS", Convert.rsAccount(accountId));
    }

    private JSONData() {} // never

}
