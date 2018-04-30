package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.DateRenderer;
import xyz.cleangone.data.aws.dynamo.dao.CatalogItemDao;
import xyz.cleangone.data.aws.dynamo.entity.bid.UserBid;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.*;
import xyz.cleangone.data.manager.event.BidManager;
import xyz.cleangone.data.manager.event.ItemManager;
import xyz.cleangone.e2.web.manager.EntityChangeManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.org.BasePage;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.BidHandler;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.CatalogView;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.ItemPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.CatalogLayout;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import java.util.*;
import java.util.stream.Collectors;

public class WatchLayout extends BaseAdmin implements CatalogView
{
    private BasePage page;
    private BidManager bidMgr;
    private EventManager eventMgr;
    private ItemManager itemMgr;
    private User user;
    private BidHandler bidHandler;

    public WatchLayout(BasePage page, MessageDisplayer msgDisplayer)
    {
        super(msgDisplayer);
        this.page = page;

        setMargin(new MarginInfo(true, false, false, false)); // T/R/B/L margins
        setSpacing(false);
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
        CatalogLayout catalogLayout = new CatalogLayout(visibleItems.size(), eventMgr, bidMgr, eventsById);
        for (CatalogItem item : visibleItems)
        {
            catalogLayout.addItem(item, user, bidHandler.getQuickBidButton(item, eventsById.get(item.getEventId())));
        }

        addComponent(catalogLayout);
    }

    public void schedule(Runnable runnable)
    {
        page.schedule(runnable);
    }
}
