package xyz.cleangone.e2.web.vaadin.desktop.org;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.FooterRow;
import org.vaadin.dialogs.ConfirmDialog;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.item.CartItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.purchase.Cart;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.ItemPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.payment.IatsPaymentPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.payment.PaymentPage;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.List;
import java.util.logging.Logger;

import static xyz.cleangone.data.aws.dynamo.entity.item.CartItem.CART_ITEM_NAME_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.item.CartItem.PRICE_FIELD;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createDeleteButton;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createTextButton;


public class CartPage extends BasePage implements View
{
    private static final Logger LOG = Logger.getLogger(CartPage.class.getName());
    public static final String NAME = "Cart";

    private Panel cartPanel = new Panel();
    private VerticalLayout cartLayout = new VerticalLayout();

    private EventManager eventMgr;
    private Cart cart;

    public CartPage()
    {
        // todo - extend singlebannerpage w/ diff cols?
        super(BannerStyle.Single);

        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");

        cartPanel.setContent(cartLayout);
    }

    protected PageDisplayType set(SessionManager sessionMgr)
    {
        super.set(sessionMgr);
        eventMgr = sessionMgr.getEventManager();
        cart = sessionMgr.getCart();

        return set();
    }

    protected PageDisplayType set()
    {
        resetHeader();

        cartLayout.removeAllComponents();
        cartLayout.addComponent(getCartGrid());

        mainLayout.removeAllComponents();
        mainLayout.addComponent(cartPanel);

        String pageName = orgMgr.getOrg().isPaymentProcessor(Organization.PaymentProcessorType.iATS) ? IatsPaymentPage.NAME : PaymentPage.NAME;

        // todo - check if items still all available
        if (!cart.isEmpty()) { mainLayout.addComponent(createTextButton("Checkout", e -> navigateTo(pageName))); }

        return PageDisplayType.NotApplicable;
    }

    private Grid<CartItem> getCartGrid()
    {
        Grid<CartItem> grid = new Grid<>();
        grid.setSizeFull();

        grid.addComponentColumn(this::buildNameLinkButton)
            .setId(CART_ITEM_NAME_FIELD.getName()).setCaption(CART_ITEM_NAME_FIELD.getDisplayName()).setExpandRatio(5);
        addColumn(grid, PRICE_FIELD, CartItem::getDisplayPrice, 1);
        grid.addComponentColumn(this::buildDeleteButton);

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

    private Button buildDeleteButton(CartItem item)
    {
        return createDeleteButton("Delete Item", e -> {
            ConfirmDialog.show(getUI(), "Confirm Item Delete", "Delete item '" + item.getName() + "'?",
                "Delete", "Cancel", new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            cart.removeItem(item);
                            set();
                        }
                    }
                });
        });
    }

    private Button buildNameLinkButton(CartItem cartItem)
    {
        return VaadinUtils.createLinkButton(cartItem.getName(), e -> handleNameLink(cartItem));
    }

    private void handleNameLink(CartItem cartItem)
    {
        eventMgr.setEvent(cartItem.getEvent());

        if (cartItem.isDonationOrPledgeFulfillment())
        {
            navigateTo(EventPage.NAME);
        }
        else
        {
            eventMgr.setEvent(cartItem.getEvent());
            eventMgr.setCategory(cartItem.getCategory());
            eventMgr.setItem(cartItem.getCatalogItem());
            navigateTo(ItemPage.NAME);
        }
    }
}