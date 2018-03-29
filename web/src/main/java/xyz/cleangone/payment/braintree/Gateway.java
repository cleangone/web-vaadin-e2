package xyz.cleangone.payment.braintree;

import com.braintreegateway.*;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.payment.PaymentResult;

import java.math.BigDecimal;

public class Gateway
{
    private static String merchantId = "y5gr23fv42kg59mk";
    private static String publicKey = "mxs4ph6htr4nqyrt";
    private static String privateKey = "9c65fe345cb3b5f4830ebccdee0e08ab";

    private final Organization org;
    private final BraintreeGateway braintreeGateway;

    public Gateway(Organization org)
    {
        this.org = org;
        braintreeGateway = new BraintreeGateway(Environment.SANDBOX, merchantId, publicKey, privateKey);
    }

    public PaymentResult submitTransaction(BigDecimal amount, String nonce)
    {
        TransactionRequest request = new TransactionRequest()
            .amount(amount)
            .paymentMethodNonce(nonce)
            .options()
            .submitForSettlement(true)
            .done();

        Result<Transaction> result = braintreeGateway.transaction().sale(request);
        if (result.isSuccess())
        {
            Transaction transaction = result.getTarget();
            //System.out.println("Success!: " + transaction.getId());
            return new PaymentResult(PaymentResult.PaymentStatus.Success);
        }

        String msg = "";
        if (result.getTransaction() != null)
        {
            Transaction transaction = result.getTransaction();
            msg = transaction.getProcessorResponseText();
            System.out.println("Error processing transaction:");
            System.out.println("  Status: " + transaction.getStatus());
            System.out.println("  Code: " + transaction.getProcessorResponseCode());
            System.out.println("  Text: " + transaction.getProcessorResponseText());
        }
        else
        {
            for (ValidationError error : result.getErrors().getAllDeepValidationErrors())
            {
                msg += error.getMessage() + " ";
                System.out.println("Attribute: " + error.getAttribute());
                System.out.println("  Code: " + error.getCode());
                System.out.println("  Message: " + error.getMessage());
            }
        }

        return new PaymentResult(PaymentResult.PaymentStatus.Failure, msg);
    }
}
