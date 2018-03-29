package xyz.cleangone.payment.braintree;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;
import elemental.json.JsonArray;

import java.io.Serializable;
import java.util.ArrayList;

@JavaScript({
    "braintree_client.js",
    "braintree_client_connector.js",
    "https://js.braintreegateway.com/web/dropin/1.9.2/js/dropin.min.js" })
public class BraintreeClient extends AbstractJavaScriptComponent
{
    private ArrayList<ValueChangeListener> listeners = new ArrayList<ValueChangeListener>();

    public BraintreeClient()
    {
        // onclick sets nonce and notifies listeners
        addFunction("onClick", new JavaScriptFunction() {
            public void call(JsonArray arguments) {
                getState().setResult(new BraintreeClientResult(arguments.getString(0), arguments.getString(1)));
                notifyListeners();
            }
        });
    }

    // component side can set the auth and get the nonce
    public void setAuth(String auth)
    {
        getState().setAuth(auth);
    }
    public BraintreeClientResult getResult()
    {
        return getState().getResult();
    }

    public interface ValueChangeListener extends Serializable { void valueChange(); }
    public void addValueChangeListener(ValueChangeListener listener)
    {
        listeners.add(listener);
    }
    public void notifyListeners() { listeners.forEach(ValueChangeListener::valueChange); }

    @Override
    protected BraintreeClientState getState()
    {
        return (BraintreeClientState)super.getState();
    }
}