package xyz.cleangone.e2.web.vaadin.desktop.org.payment;

import com.vaadin.navigator.View;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.web.vaadin.util.VaadinUtils;
import xyz.cleangone.payment.PaymentResult;
import xyz.cleangone.payment.iats.IatsClient;

public class IatsPaymentPage extends PaymentPage implements View
{
    public static final String NAME = "IatsPayment";

    private TextField ccField = VaadinUtils.createTextField("Credit Card");
    private TextField expirationDateField = VaadinUtils.createTextField("Expiration Date");
    private TextField cvvField = VaadinUtils.createTextField("CVV");

    private IatsClient iatsClient;

    protected PageDisplayType set(SessionManager sessionMgr)
    {
        iatsClient = new IatsClient(sessionMgr.getOrgManager().getPaymentProcessor());

        return super.set(sessionMgr);
    }

    protected void setForm()
    {
        super.setForm();

        formLayout.addComponent(ccField);
        formLayout.addComponent(expirationDateField);
        formLayout.addComponent(cvvField);
    }

    protected boolean canCheckout()
    {
        return (super.canCheckout() && iatsClient != null);
    }

    protected Component getCheckout()
    {
        String invoiceNumber = "test";

        Button button = getCheckoutButton();
        button.addClickListener(e -> {
            PaymentResult paymentResult = iatsClient.chargeCreditCard(invoiceNumber,
                ccField.getValue(), expirationDateField.getValue(), person.getFirstLast(), person.getLastName(), address, cvvField.getValue(),
                cart.getTotal().toString());

            handlePaymentResult(paymentResult);
        });

        return button;
    }
}
