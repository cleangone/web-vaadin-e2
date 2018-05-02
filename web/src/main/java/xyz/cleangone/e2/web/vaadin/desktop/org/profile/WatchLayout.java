package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.*;
import xyz.cleangone.data.manager.event.BidManager;
import xyz.cleangone.data.manager.event.ItemManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.MyUI;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.org.BasePage;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.BidHandler;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.CatalogView;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.CatalogLayout;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import java.util.*;
import java.util.stream.Collectors;

public class WatchLayout extends BaseAdmin implements CatalogView
{
    private BasePage page;
    private int pageWidth;
    private BidManager bidMgr;
    private EventManager eventMgr;
    private ItemManager itemMgr;
    private User user;
    private BidHandler bidHandler;
    private CatalogLayout catalogLayout;

    public WatchLayout(BasePage page, int pageWidth, MessageDisplayer msgDisplayer)
    {
        super(msgDisplayer);
        this.page = page;
        this.pageWidth = pageWidth;

        setMargin(false);
        setSpacing(false);
        if (MyUI.COLORS) { addStyleName("backGreen"); }
    }

    public void set(SessionManager sessionMgr)
    {
        OrgManager orgMgr = sessionMgr.getOrgManager();
        eventMgr = sessionMgr.getEventManager();
        bidMgr = orgMgr.getBidManager();
        itemMgr = orgMgr.getItemManager();
        user = sessionMgr.getPopulatedUserManager().getUser();
        bidHandler = new BidHandler(this, sessionMgr, msgDisplayer);

        set();
    }

    public void set()
    {
        setCatalogLayout();
    }
    public void setCatalogLayout()
    {
        removeAllComponents();

        List<CatalogItem> items = itemMgr.getItems(user.getWatchedItemIds());
        List<CatalogItem> visibleItems = items.stream()
            .filter(CatalogItem::isVisible)
            .collect(Collectors.toList());

        Map<String, OrgEvent> eventsById = eventMgr.getEventsById();
        catalogLayout = new CatalogLayout(pageWidth, eventMgr, bidMgr, eventsById);
        for (CatalogItem item : visibleItems)
        {
            catalogLayout.addItem(item, user, bidHandler.getQuickBidButton(item, eventsById.get(item.getEventId())));
        }

        addComponent(catalogLayout);
    }

    public void resetPageWidth(int pageWidth)
    {
        this.pageWidth = pageWidth;
        if (catalogLayout != null) { catalogLayout.resetPageWidth(pageWidth); }
    }

    public void schedule(Runnable runnable)
    {
        page.schedule(runnable);
    }
}
