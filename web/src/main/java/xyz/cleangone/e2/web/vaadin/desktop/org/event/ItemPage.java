package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.vaadin.navigator.View;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.bid.ItemBid;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.item.CartItem;
import xyz.cleangone.data.aws.dynamo.entity.item.PurchaseItem;
import xyz.cleangone.data.aws.dynamo.entity.purchase.Cart;
import xyz.cleangone.data.manager.ActionManager;
import xyz.cleangone.data.manager.event.BidManager;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageDimension;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageLabel;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.util.DollarField;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.math.BigDecimal;
import java.util.List;

public class ItemPage extends CatalogPage implements View
{
    public static final String NAME = "Item";
    private BidManager bidManager = new BidManager();

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
        ItemBid highBid = item.getHighBidId() == null ? null : bidManager.getItemBid(item.getHighBidId());

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

        String displayPrice = item.getDisplayPrice();
        if (highBid != null && highBid.getUserId().equals(user.getId())) { displayPrice += " (you are high bidder)"; }
        detailslayout.addComponent(new Label(displayPrice));

        Integer quantity = item.getQuantity();
        if (quantity != null)
        {
            detailslayout.addComponent(quantity == 0 ? EventUtils.getSoldLabel() : new Label(quantity + " remaining"));
        }

        if (quantity == null || quantity > 0)
        {
            if (item.getSaleType() == PurchaseItem.SaleType.Bid)
            {
                DollarField maxBidField = new DollarField("Max Bid");
                detailslayout.addComponent(maxBidField);
                detailslayout.addComponent(VaadinUtils.createTextButton("Bid", ev ->
                {
                    BigDecimal maxBid = maxBidField.getDollarValue();
                    if (maxBid.compareTo(item.getPrice()) > 0)
                    {
                        bidManager.createBid(user, item, maxBid);
                        ActionManager actionMgr = orgMgr.getActionManager();
                        Action bid = actionMgr.createBid(user, item, event);
                        actionMgr.save(bid);
                        actionBar.displayMessage("Bid submitted");
                        setCenterLayout();
                    }
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