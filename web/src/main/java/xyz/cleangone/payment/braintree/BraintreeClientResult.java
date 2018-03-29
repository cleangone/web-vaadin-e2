package xyz.cleangone.payment.braintree;

public class BraintreeClientResult
{
    private String nonce;
    private String error;

    public BraintreeClientResult(String nonce, String error)
    {
        this.nonce = nonce;
        this.error = error;
    }

    public String getNonce()
    {
        return nonce;
    }
    public String getError()
    {
        return error;
    }

}