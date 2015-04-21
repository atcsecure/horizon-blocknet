package nhz.user;

import nhz.Account;
import nhz.Attachment;
import nhz.Constants;
import nhz.Nhz;
import nhz.NhzException;
import nhz.Transaction;
import nhz.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static nhz.user.JSONResponses.NOTIFY_OF_ACCEPTED_TRANSACTION;

public final class SendMoney extends UserServlet.UserRequestHandler {

    static final SendMoney instance = new SendMoney();

    private SendMoney() {}

    @Override
    JSONStreamAware processRequest(HttpServletRequest req, User user) throws NhzException.ValidationException, IOException {
        if (user.getSecretPhrase() == null) {
            return null;
        }

        String recipientValue = req.getParameter("recipient");
        String amountValue = req.getParameter("amountNHZ");
        String feeValue = req.getParameter("feeNHZ");
        String deadlineValue = req.getParameter("deadline");
        String secretPhrase = req.getParameter("secretPhrase");

        Long recipient;
        long amountNQT = 0;
        long feeNQT = 0;
        short deadline = 0;

        try {

            recipient = Convert.parseUnsignedLong(recipientValue);
            if (recipient == null) throw new IllegalArgumentException("invalid recipient");
            amountNQT = Convert.parseNHZ(amountValue.trim());
            feeNQT = Convert.parseNHZ(feeValue.trim());
            deadline = (short)(Double.parseDouble(deadlineValue) * 60);

        } catch (RuntimeException e) {

            JSONObject response = new JSONObject();
            response.put("response", "notifyOfIncorrectTransaction");
            response.put("message", "One of the fields is filled incorrectly!");
            response.put("recipient", recipientValue);
            response.put("amountNHZ", amountValue);
            response.put("feeNHZ", feeValue);
            response.put("deadline", deadlineValue);

            return response;
        }

        if (! user.getSecretPhrase().equals(secretPhrase)) {

            JSONObject response = new JSONObject();
            response.put("response", "notifyOfIncorrectTransaction");
            response.put("message", "Wrong secret phrase!");
            response.put("recipient", recipientValue);
            response.put("amountNHZ", amountValue);
            response.put("feeNHZ", feeValue);
            response.put("deadline", deadlineValue);

            return response;

        } else if (amountNQT <= 0 || amountNQT > Constants.MAX_BALANCE_NQT) {

            JSONObject response = new JSONObject();
            response.put("response", "notifyOfIncorrectTransaction");
            response.put("message", "\"Amount\" must be greater than 0!");
            response.put("recipient", recipientValue);
            response.put("amountNHZ", amountValue);
            response.put("feeNHZ", feeValue);
            response.put("deadline", deadlineValue);

            return response;

        } else if (feeNQT < Constants.ONE_NHZ || feeNQT > Constants.MAX_BALANCE_NQT) {

            JSONObject response = new JSONObject();
            response.put("response", "notifyOfIncorrectTransaction");
            response.put("message", "\"Fee\" must be at least 1 NHZ!");
            response.put("recipient", recipientValue);
            response.put("amountNHZ", amountValue);
            response.put("feeNHZ", feeValue);
            response.put("deadline", deadlineValue);

            return response;

        } else if (deadline < 1 || deadline > 1440) {

            JSONObject response = new JSONObject();
            response.put("response", "notifyOfIncorrectTransaction");
            response.put("message", "\"Deadline\" must be greater or equal to 1 minute and less than 24 hours!");
            response.put("recipient", recipientValue);
            response.put("amountNHZ", amountValue);
            response.put("feeNHZ", feeValue);
            response.put("deadline", deadlineValue);

            return response;

        }

        Account account = Account.getAccount(user.getPublicKey());
        if (account == null || Convert.safeAdd(amountNQT, feeNQT) > account.getUnconfirmedBalanceNQT()) {

            JSONObject response = new JSONObject();
            response.put("response", "notifyOfIncorrectTransaction");
            response.put("message", "Not enough funds!");
            response.put("recipient", recipientValue);
            response.put("amountNHZ", amountValue);
            response.put("feeNHZ", feeValue);
            response.put("deadline", deadlineValue);

            return response;

        } else {

            final Transaction transaction = Nhz.getTransactionProcessor().newTransactionBuilder(user.getPublicKey(),
                    amountNQT, feeNQT, deadline, Attachment.ORDINARY_PAYMENT).recipientId(recipient).build();
            transaction.validate();
            transaction.sign(user.getSecretPhrase());

            Nhz.getTransactionProcessor().broadcast(transaction);

            return NOTIFY_OF_ACCEPTED_TRANSACTION;

        }
    }

    @Override
    boolean requirePost() {
        return true;
    }

}
