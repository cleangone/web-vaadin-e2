package xyz.cleangone.e2.web.vaadin.desktop.org.payment;

import com.vaadin.navigator.View;
import com.vaadin.ui.Component;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.payment.PaymentResult;
import xyz.cleangone.payment.braintree.BraintreeClient;
import xyz.cleangone.payment.braintree.BraintreeClientResult;
import xyz.cleangone.payment.braintree.Gateway;

import java.math.BigDecimal;

public class BraintreePaymentPage extends PaymentPage implements View
{
    private BraintreeClient braintreeClient;
    private Gateway gateway;

    protected void set(SessionManager sessionMgr)
    {
        braintreeClient = new BraintreeClient();
        braintreeClient.setAuth("sandbox_fd8c7ggz_y5gr23fv42kg59mk");  // todo - get fm org

        gateway = new Gateway(sessionMgr.getOrg());

        super.set(sessionMgr);
    }

    protected Component getCheckout()
    {
        braintreeClient.addValueChangeListener(new BraintreeClient.ValueChangeListener() {
            public void valueChange()
            {
                BraintreeClientResult result = braintreeClient.getResult();
                String nonce = result.getNonce();

                // submit test nonce to gateway
                // todo - obviouisly chg hardcoded $10, used for paypal
                PaymentResult paymentResult = gateway.submitTransaction(new BigDecimal("10.00"), nonce);
                handlePaymentResult(paymentResult);
            }
        });

        return braintreeClient;
    }
}
