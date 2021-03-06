package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.*;
import xyz.cleangone.data.manager.event.ItemManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.org.BasePage;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.BidHandler;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.CatalogView;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.CatalogLayout;
import xyz.cleangone.web.vaadin.ui.MessageDisplayer;

import java.util.*;
import java.util.stream.Collectors;

import static xyz.cleangone.web.vaadin.util.VaadinUtils.SHOW_BACKBROUND_COLORS;

public class WatchLayout extends BaseAdmin implements CatalogView
{
    private BasePage page;
    private int pageWidth;

    private SessionManager sessionMgr;
    private ItemManager itemMgr;
    private User user;
    private BidHandler bidHandler;

    Map<String, OrgEvent> eventsById;
    private CatalogLayout catalogLayout;

    public WatchLayout(BasePage page, int pageWidth, MessageDisplayer msgDisplayer)
    {
        super(msgDisplayer);
        this.page = page;
        this.pageWidth = pageWidth;

        setMargin(false);
        setSpacing(false);
        if (SHOW_BACKBROUND_COLORS) { addStyleName("backGreen"); }
    }

    public void set(SessionManager sessionMgr)
    {
        this.sessionMgr = sessionMgr;
        OrgManager orgMgr = sessionMgr.getOrgManager();
        itemMgr = orgMgr.getItemManager();
        user = sessionMgr.getPopulatedUserManager().getUser();
        bidHandler = new BidHandler(this, sessionMgr, msgDisplayer);
        eventsById = sessionMgr.getEventManager().getEventsById();

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

        catalogLayout = new CatalogLayout(pageWidth, sessionMgr, eventsById);
        for (CatalogItem item : visibleItems)
        {
            catalogLayout.addItem(item, user, bidHandler.getQuickBidButton(item));
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
