package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.vaadin.navigator.View;
import com.vaadin.ui.Button;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.bid.BidUtils;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.ActionManager;
import xyz.cleangone.data.manager.ImageManager;
import xyz.cleangone.data.manager.event.BidManager;
import xyz.cleangone.data.manager.event.BidStatus;
import xyz.cleangone.e2.web.manager.OutbidEmailSender;
import xyz.cleangone.e2.web.vaadin.desktop.broadcast.Broadcaster;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.CatalogLayout;
import xyz.cleangone.e2.web.vaadin.util.DollarField;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createTextButton;

public class CatalogPage extends BaseEventPage implements View
{
    public static final String NAME = "Catalog";

    protected ImageManager imageMgr;
    protected BidManager bidManager;
    protected OrgTag category;
    Set<String> itemIds = Collections.emptySet();

    public CatalogPage()
    {
         super(PageCols.Left, PageCols.Center);
    }

    public PageDisplayType set()
    {
        bidManager = orgMgr.getBidManager();
        category = eventMgr.getCategory();
        if (category == null) { return PageDisplayType.NotApplicable; }  // shouldn't happen - nav back to event?

        imageMgr = itemMgr.getImageManager();

        leftLayout.set(category);
        setCenterLayout();

        return PageDisplayType.NotApplicable;
    }

    public boolean hasItemId(String itemId)
    {
        return (itemIds.contains(itemId));
    }

    protected void setCenterLayout()
    {
        centerLayout.removeAllComponents();

        List<CatalogItem> items = itemMgr.getItems(category.getId());
        List<CatalogItem> visibleItems = items.stream()
            .filter(CatalogItem::isVisible)
            .collect(Collectors.toList());
        itemIds = visibleItems.stream()
            .map(CatalogItem::getId)
            .collect(Collectors.toSet());

        CatalogLayout catalogLayout = new CatalogLayout(visibleItems.size(), eventMgr, bidManager, imageMgr);
        for (CatalogItem item : visibleItems)
        {
            catalogLayout.addItem(item, user, getQuickBidButton(item));
        }

        centerLayout.addComponent(catalogLayout);
    }

    protected Button getQuickBidButton(CatalogItem item)
    {
        BigDecimal bidAmount = BidUtils.getIncrementedAmount(item.getPrice());
        return createTextButton("Bid $" + bidAmount, ev -> handleBid(item, bidAmount));
    }

    protected void handleWatch(CatalogItem item, boolean watch)
    {
        if (watch) { user.addWatchedItemId(item.getId()); }
        else  { user.removeWatchedItemId(item.getId()); }

        userMgr.saveUser();
        setCenterLayout();
    }

    protected void handleBid(CatalogItem item, DollarField maxBidField)
    {
        handleBid(item, maxBidField.getDollarValue());
    }
    protected void handleBid(CatalogItem item, BigDecimal maxBid)
    {
        if (maxBid.compareTo(item.getPrice()) > 0)
        {
            // note - item will be updated in-place with any new bid
            BidStatus bidStatus = bidManager.createBid(user, item, maxBid);

            if (bidStatus.getUserBid() != null)
            {
                ActionManager actionMgr = orgMgr.getActionManager();
                Action bid = actionMgr.createBid(user, bidStatus.getUserBid(), item, event);
                actionMgr.save(bid);
                actionBar.displayMessage("Bid submitted");

                if (bidStatus.getPreviousHighBid() != null)
                {
                    schedule(new OutbidEmailSender(bidStatus.getPreviousHighBid(), sessionMgr));
                }

                setCenterLayout();
            }
        }

        Broadcaster.broadcast(item);
    }


}