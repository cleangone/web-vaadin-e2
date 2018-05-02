package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.vaadin.navigator.View;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.bid.BidUtils;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.ActionManager;
import xyz.cleangone.data.manager.ImageManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.data.manager.event.BidManager;
import xyz.cleangone.data.manager.event.BidStatus;
import xyz.cleangone.e2.web.manager.OutbidEmailSender;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.broadcast.Broadcaster;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.CatalogLayout;
import xyz.cleangone.e2.web.vaadin.util.DollarField;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createTextButton;

public class CatalogPage extends BaseEventPage implements View, CatalogView
{
    public static final String NAME = "Catalog";

    protected ImageManager imageMgr;
    protected BidManager bidManager;
    protected OrgTag category;
    private CatalogLayout catalogLayout;

    protected BidHandler bidHandler;
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

        bidHandler = new BidHandler(this, sessionMgr, actionBar);

        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> resetPageWidth());

        leftLayout.set(category);
        setCenterLayout();

        return PageDisplayType.NotApplicable;
    }

    public boolean hasItemId(String itemId)
    {
        return (itemIds.contains(itemId));
    }

    public void setCatalogLayout()
    {
        setCenterLayout();
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

        catalogLayout = new CatalogLayout(UI.getCurrent().getPage().getBrowserWindowWidth(), eventMgr, bidManager);
        for (CatalogItem item : visibleItems)
        {
            catalogLayout.addItem(item, user, getQuickBidButton(item));
        }

        centerLayout.addComponent(catalogLayout);
    }

    private void resetPageWidth()
    {
        catalogLayout.resetPageWidth(UI.getCurrent().getPage().getBrowserWindowWidth());
    }

    protected Button getQuickBidButton(CatalogItem item)
    {
        return bidHandler.getQuickBidButton(item, event);
    }
    protected void handleWatch(CatalogItem item, boolean watch)
    {
        bidHandler.handleWatch(item, watch);
    }
    protected void handleBid(CatalogItem item, DollarField maxBidField)
    {
        handleBid(item, maxBidField.getDollarValue());
    }
    protected void handleBid(CatalogItem item, BigDecimal maxBid)
    {
        bidHandler.handleBid(item, event, maxBid);
    }

}