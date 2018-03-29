window.xyz_cleangone_payment_braintree_BraintreeClient = function()
{
    //alert("In braintree_client_connector.js");

    var braintreeJsClient = new braintreelibrary.BraintreeJsClient(this.getElement());

    // Handle changes from the server-side
    this.onStateChange = function()
    {
        braintreeJsClient.setAuth(this.getState().auth);
    };

    // Pass user interaction to the server-side
    var self = this;
    braintreeJsClient.click = function()
    {
        self.onClick(braintreeJsClient.getNonce(), braintreeJsClient.getError());
    };
};