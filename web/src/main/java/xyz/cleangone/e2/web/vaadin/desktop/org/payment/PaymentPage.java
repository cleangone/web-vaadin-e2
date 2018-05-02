package xyz.cleangone.e2.web.vaadin.desktop.org.payment;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.FooterRow;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.item.CartItem;
import xyz.cleangone.data.aws.dynamo.entity.person.Address;
import xyz.cleangone.data.aws.dynamo.entity.person.Person;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.aws.dynamo.entity.purchase.Cart;
import xyz.cleangone.data.manager.ActionManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.BasePage;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.payment.PaymentResult;

import java.util.List;
import java.util.logging.Logger;
import static xyz.cleangone.data.aws.dynamo.entity.item.CartItem.*;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;


public class PaymentPage extends BasePage implements View
{
    private static final Logger LOG = Logger.getLogger(PaymentPage.class.getName());
    public static final String NAME = "Payment";

    private VerticalLayout cartLayout = new VerticalLayout();
    protected FormLayout formLayout = new FormLayout();

//    protected CheckoutProcessor checkoutProcessor;
    protected Cart cart;
    protected User user;
    protected Person person;
    protected Address address;

    public PaymentPage()
    {
        // todo - extend singlebannerpage w/ diff cols?
        super(BannerStyle.Single);

        mainLayout.setMargin(true);
        mainLayout.setWidth("100%");

        Panel cartPanel = new Panel();
        cartPanel.setContent(cartLayout);

        mainLayout.addComponents(cartPanel, formLayout);
    }

    protected PageDisplayType set(SessionManager sessionMgr)
    {
        super.set(sessionMgr);
        UserManager userMgr = sessionMgr.getUserManager();

        user = userMgr.copyUser();  // copy so one-time changes do not affect profile
        if (user == null)
        {
            user = new User();
            user.setPerson(new Person());
        }

        person = user.getPerson();
        address = userMgr.copyAddress();

        cart = sessionMgr.getCart();

        return set();
    }

    protected PageDisplayType set()
    {
        resetHeader();

        cartLayout.removeAllComponents();
        formLayout.removeAllComponents();

        cartLayout.addComponent(getCartGrid());

        setForm();

        if (canCheckout()) { formLayout.addComponent(getCheckout()); }

        return PageDisplayType.NotApplicable;
    }

    protected void setForm()
    {
        // changes in these fields affect checkout but are not yet saved for a logged-in user
        formLayout.addComponent(createTextField(Person.FIRST_NAME_FIELD, person));
        formLayout.addComponent(createTextField(Person.LAST_NAME_FIELD,  person));
        formLayout.addComponent(createTextField(User.EMAIL_FIELD, user));

        formLayout.addComponent(createTextField(Address.ADDRESS_FIELD, address));
        formLayout.addComponent(createTextField(Address.CITY_FIELD,    address));
        formLayout.addComponent(createTextField(Address.STATE_FIELD,   address));
        formLayout.addComponent(createTextField(Address.ZIP_FIELD,     address));
    }

    protected Button getCheckoutButton()
    {
        String caption = cart.getButtonCaption();
        return createTextButton(caption);
     }

    protected boolean canCheckout()
    {
        return !cart.isEmpty();
    }

    protected Component getCheckout()
    {
        // placeholder button w/o processing
        Button button = getCheckoutButton();
        button.addClickListener(e -> handlePaymentResult(new PaymentResult(PaymentResult.PaymentStatus.Success)));
        return button;
    }

    protected void handlePaymentResult(PaymentResult paymentResult)
    {
        if (paymentResult.isSuccess())
        {
            ActionManager actionMgr = orgMgr.getActionManager();
            actionMgr.createActions(user, cart);

            sessionMgr.setMsg(cart.getSuccessMsg());
            cart.clear();
            getUI().getNavigator().navigateTo(cart.getReturnPage());
        }
        else
        {
            Notification.show("Error processing credit card: " + paymentResult.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }


    private Grid<CartItem> getCartGrid()
    {
        Grid<CartItem> grid = new Grid<>();
        grid.setSizeFull();

        addColumn(grid, CART_ITEM_NAME_FIELD, CartItem::getName, 5);
        addColumn(grid, PRICE_FIELD, CartItem::getDisplayPrice, 1);

        FooterRow footerRow = grid.appendFooterRow();
        footerRow.getCell(CART_ITEM_NAME_FIELD.getName()).setHtml("<div align=right><b>Total</b></div>");
        footerRow.getCell(PRICE_FIELD.getName()).setHtml("<b>" + cart.getDisplayTotal() + "</b>");

        List<CartItem> items = cart.getItems();

        grid.setHeightByRows(items.size() > 0 ? items.size() : 1);
        grid.setDataProvider(new ListDataProvider<>(items));

        return grid;
    }

    private void addColumn(
        Grid<CartItem> grid, EntityField entityField, ValueProvider<CartItem, String> valueProvider, int expandRatio)
    {
        grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName()).setExpandRatio(expandRatio);
    }

    public TextField createTextField(EntityField field, BaseEntity entity)
    {
        TextField textField = VaadinUtils.createTextField(field.getDisplayName(), entity.get(field));
        textField.addValueChangeListener(event -> entity.set(field, (String)event.getValue()));

        return textField;
    }
}