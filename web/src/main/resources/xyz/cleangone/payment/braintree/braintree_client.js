var braintreelibrary = braintreelibrary || {};

braintreelibrary.BraintreeJsClient = function (element)
{
    element.innerHTML =
        "<div id='braintree-dropin-container'></div>" +
        "<button id='braintree-submit-button'>Purchase</button>";

    var self = this;
    var authSet = false;
    var paymentNonce = "";
    var paymentError = "";

    this.setAuth = function(auth)
    {
        if (authSet) return;

        var button = document.querySelector('#braintree-submit-button');
        braintree.dropin.create({
            // authorization: 'sandbox_fd8c7ggz_y5gr23fv42kg59mk',
            authorization: auth,
            container: '#braintree-dropin-container',
            paypal: {
                flow: 'checkout',
                amount: '10.00',
                currency: 'USD'
            }
        }).then(function (dropinInstance) {
            button.addEventListener('click', function () {
                //alert("requesting payment method");
                dropinInstance.requestPaymentMethod().then(function (payload) {
                    //alert("Recvd nonce: " + payload.nonce);
                    paymentNonce = payload.nonce;
                    paymentError = "";
                    self.click();
                }).catch(function (requestPaymentMethodError) {
                    //alert("RequestPaymentMethod Error: " + requestPaymentMethodError);
                    paymentError = requestPaymentMethodError;
                    paymentNonce = "";
                });
            });
        }).catch(function (createErr) {
            alert("Create Error: " + createErr);
        });

        authSet = true;
    };

    this.getNonce = function() { return paymentNonce };
    this.getError = function() { return paymentError };

    // click action defined in <component>_connector.js
    this.click = function() { alert("Error: Must implement click() method"); };
};