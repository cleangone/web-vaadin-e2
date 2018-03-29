package xyz.cleangone.payment.braintree;

import com.vaadin.shared.ui.JavaScriptComponentState;

public class BraintreeClientState extends JavaScriptComponentState
{
    private String auth;
    private BraintreeClientResult result;

    public String getAuth()
    {
        return auth;
    }
    public void setAuth(String auth)
    {
        this.auth = auth;
    }

    public BraintreeClientResult getResult()
    {
        return result;
    }
    public void setResult(BraintreeClientResult result)
    {
        this.result = result;
    }
}