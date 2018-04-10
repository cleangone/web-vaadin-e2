package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.vaadin.navigator.View;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.item.CartItem;
import xyz.cleangone.data.aws.dynamo.entity.item.PurchaseItem;
import xyz.cleangone.data.aws.dynamo.entity.purchase.Cart;
import xyz.cleangone.data.manager.ActionManager;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageDimension;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageLabel;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.List;

public class ItemPage extends CatalogPage implements View
{
    public static final String NAME = "Item";

    protected PageDisplayType set()
    {
        imageMgr = itemMgr.getImageManager();

        leftLayout.set(category);
        setCenterLayout();

        return PageDisplayType.NotApplicable;
    }

    protected void setCenterLayout()
    {
        centerLayout.removeAllComponents();

        CatalogItem item = itemMgr.getItem();

        centerLayout.addComponent(getItemLayout(item));
    }

    private Component getItemLayout(CatalogItem item)
    {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setMargin(new MarginInfo(true, false, false, false)); // T/R/B/L margins

        List<S3Link> images = item.getImages();
        if (images != null && !images.isEmpty())
        {
            String imageUrl = imageMgr.getUrl(images.get(0));
            ImageLabel imageLabel = new ImageLabel(imageUrl, ImageDimension.width(400)).withHref();
            layout.addComponent(imageLabel);
        }

        VerticalLayout detailslayout = new VerticalLayout();
        detailslayout.setMargin(false);
        layout.addComponent(detailslayout);

        detailslayout.addComponent(new Label(item.getName()));
        detailslayout.addComponent(new Label(item.getDisplayPrice()));

        Integer quantity = item.getQuantity();
        if (quantity != null)
        {
            detailslayout.addComponent(quantity == 0 ? EventUtils.getSoldLabel() : new Label(quantity + " remaining"));
        }

        if (quantity == null || quantity > 0)
        {
            if (item.getSaleType() == PurchaseItem.SaleType.Bid)
            {
                detailslayout.addComponent(VaadinUtils.createTextButton("Bid", ev ->
                {
                    ActionManager actionMgr = orgMgr.getActionManager();
                    Action bid = actionMgr.createBid(user, item, event);
                    actionMgr.save(bid);
                    actionBar.displayMessage("Bid submitted");
                }));
            }
            else if (item.getSaleType() == PurchaseItem.SaleType.Purchase)
            {
                detailslayout.addComponent(VaadinUtils.createTextButton("Purchase", ev ->
                {
                    Cart cart = sessionMgr.getCart();
                    cart.addItem(new CartItem(item, event, category));
                    cart.setReturnPage(EventPage.NAME);

                    actionBar.setCartMenuItem();
                    actionBar.displayMessage("Item added to Cart");
                }));
            }
        }

        return layout;
    }
}