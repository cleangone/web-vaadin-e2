package xyz.cleangone.payment.iats;

import com.iatspayments.www.NetGate.*;
import xyz.cleangone.payment.PaymentResult;

import java.util.HashMap;
import java.util.Map;


public class IatsClient
{
    public enum CardType { VISA }

    private String agentCode; // = "TEST88";
    private String password; // = "TEST88";

    public IatsClient(String agentCode, String password)
    {
        this.agentCode = agentCode;
        this.password = password;
    }

    public CreateCustomerCodeAndProcessCreditCardV1 createCustomerCode(
        String customerIpAddress, String invoiceNum, String ccNumber, String ccExpirationDate,
        String firstName, String lastName, String address, String city, String state, String zip,
        String cvv2, String total)
    {
        return new CreateCustomerCodeAndProcessCreditCardV1(
            agentCode, password, customerIpAddress, invoiceNum, ccNumber, ccExpirationDate,
            firstName, lastName, address, city, state, zip,
            cvv2, total);
    }

    public PaymentResult chargeCreditCard(
        String invoiceNum, String ccNumber, String ccExpirationDate,
        String firstName, String lastName, String address, String city, String state, String zip,
        String cvv2, String total)
    {
        try
        {
            String mop = CardType.VISA.toString();
            ProcessCreditCardV1 processCC = new ProcessCreditCardV1(
                agentCode, password, "", invoiceNum, ccNumber, ccExpirationDate, cvv2, mop,
                firstName, lastName, address, city, state, zip,
                total, "comment");

            ProcessLinkService processLinkService = new ProcessLinkService();

            IATSResponse response = processLinkService.processCreditCard(processCC);
            ProcessResult processResult = response.getProcessResult();
            String authResult = processResult.getAuthorizationResult();

            PaymentResult paymentResult = new PaymentResult(PaymentResult.PaymentStatus.Success);
            String lower = authResult.toLowerCase().trim();
            if (lower.startsWith("reject"))
            {
                paymentResult.setPaymentStatusFail();
                if (lower.contains(":"))
                {
                    String rejectCode = lower.substring(lower.indexOf(":") + 1).trim();
                    if (REJECT_CODES.containsKey(rejectCode))
                    {
                        paymentResult.setMessage(REJECT_CODES.get(rejectCode));
                    }
                }
            }

            return paymentResult;
        }
        catch (Exception e)
        {
            return new PaymentResult(PaymentResult.PaymentStatus.Failure, "Unexpected Error: " + e.getMessage());
        }
    }

    public String processRefund(String customerIpAddress, String invoiceNum, String transactionId, String total)
    {
        try
        {
            ProcessCreditCardRefundWithTransactionIdV1 refund = new ProcessCreditCardRefundWithTransactionIdV1();
            refund.setCustomerIPAddress(customerIpAddress);
            refund.setTransactionId(transactionId);
            refund.setTotal(total); // ie. "-10"
            refund.setComment("Credit Card refund test");

            ProcessLinkService processLinkService = new ProcessLinkService();
            IATSResponse response = processLinkService.processCreditCardRefundWithTransactionId(refund);

            return response.getProcessResult().getAuthorizationResult();
        }
        catch (Exception e)
        {
            return "fail";
        }
    }

    private static Map<String, String> REJECT_CODES = new HashMap<>();
    static
    {
        REJECT_CODES.put("1", "Agent code has not been set up on the authorization system.");
        REJECT_CODES.put("2", "Unable to process transaction. Verify and re-enter credit card information.");
        REJECT_CODES.put("3", "Invalid Customer Code");
        REJECT_CODES.put("4", "Incorrect Expiration date");
        REJECT_CODES.put("7", "Lost or stolen card");
        REJECT_CODES.put("8", "Invalid card status");
        REJECT_CODES.put("11", "General decline. Please call the number on the back of credit card");
        REJECT_CODES.put("12", "Incorrect CVV2 or Expiration date");
        REJECT_CODES.put("14", "The card is over the limit");
        REJECT_CODES.put("15", "General decline. Please call the number on the back of credit card");
        REJECT_CODES.put("16", "Invalid charge card number. Verify and re-enter credit card information.");
        REJECT_CODES.put("19", "Incorrect CVV2 security code");
        REJECT_CODES.put("32", "Invalid Credit Card Number");
        REJECT_CODES.put("40", "Invalid Credit Card Number");
        REJECT_CODES.put("41", "Invalid Expiration date");
        REJECT_CODES.put("42", "CVV2 required");
    }
}