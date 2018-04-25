package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.DateRenderer;
import xyz.cleangone.data.aws.dynamo.dao.CatalogItemDao;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.bid.UserBid;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.data.manager.event.BidManager;
import xyz.cleangone.e2.web.manager.EntityChangeManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.ItemPage;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.PageUtils;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity.CREATED_DATE_FIELD;

public class BidsAdmin extends BaseAdmin
{
    private BidManager bidMgr;
    private EventManager eventMgr;
    private TagManager tagMgr;
    private User user;
    private EntityChangeManager changeManager = new EntityChangeManager();

    Map<String, CatalogItem> itemIdToItem = new HashMap<>();
    Map<String, OrgEvent> itemIdToEvent   = new HashMap<>();
    Map<String, OrgTag> itemIdToCategory  = new HashMap<>();

    public BidsAdmin(MessageDisplayer msgDisplayer)
    {
        super(msgDisplayer);

        setMargin(new MarginInfo(true, false, false, false)); // T/R/B/L margins
        setSpacing(false);
    }

    public void set(SessionManager sessionMgr)
    {
        eventMgr = sessionMgr.getEventManager();
        tagMgr = sessionMgr.getOrgManager().getTagManager();
        bidMgr = sessionMgr.getOrgManager().getBidManager();
        user = sessionMgr.getPopulatedUserManager().getUser();

        set();
    }

    public void set()
    {
        CatalogItemDao dao = new CatalogItemDao();


//        if (changeManager.unchanged(user) &&
//            changeManager.unchangedEntity(user.getId()) &&
//            changeManager.unchangedEntity(user.getPersonId()))
//        {
//            return;
//        }
//
//        changeManager.reset(user);

        removeAllComponents();
        List<UserBid> bids = bidMgr.getUserBids(user);

        // events and categories cached, so retrieval not terrible
        Map<String, OrgEvent> eventIdToEvent = new HashMap<>();
        Map<String, OrgTag> categoryIdToCategory = new HashMap<>();
        for (OrgEvent event : eventMgr.getActiveEvents()) { eventIdToEvent.put(event.getId(), event); }
        for (OrgTag category : tagMgr.getCategories()) { categoryIdToCategory.put(category.getId(), category); }

        itemIdToItem.clear();
        itemIdToEvent.clear();
        itemIdToCategory.clear();

        for (UserBid bid : bids)
        {
            String itemId = bid.getItemId();
            if (!itemIdToItem.containsKey(itemId))
            {
                itemIdToItem.put(itemId, dao.getById(itemId)); // todo - hack - direct read of db
            }

            CatalogItem item = itemIdToItem.get(itemId);
            if (!itemIdToEvent.containsKey(itemId))    { itemIdToEvent.put(itemId, eventIdToEvent.get(item.getEventId())); }
            if (!itemIdToCategory.containsKey(itemId)) { itemIdToCategory.put(itemId, categoryIdToCategory.get(item.getCategoryIds().get(0))); }
        }

        Grid<UserBid> grid = new Grid<>();
        grid.setWidth("100%");

        grid.addComponentColumn(this::buildItemLinkButton).setCaption("Item");;
        grid.addColumn(this::getEndDate).setCaption("Auction End").setRenderer(new DateRenderer(PageUtils.SDF_NEXT_WEEK));
        grid.addColumn(UserBid::getDisplayMaxAmount).setCaption("Max Bid");
        grid.addColumn(UserBid::getDisplayCurrAmount).setCaption("Curr Bid");
        grid.addColumn(this::getStatus).setCaption("Status");

        if (!bids.isEmpty()) { grid.setHeightByRows(bids.size()); }
        grid.setDataProvider(new ListDataProvider<>(bids));

        addComponents(grid);
    }

    private Button buildItemLinkButton(UserBid userBid)
    {
        CatalogItem item = itemIdToItem.get(userBid.getItemId());
        if (item == null) { return VaadinUtils.createLinkButton("Unknown Item"); }

        OrgEvent event   = itemIdToEvent.get(userBid.getItemId());
        OrgTag category  = itemIdToCategory.get(userBid.getItemId());
        if (event == null || category == null) { return VaadinUtils.createLinkButton(item.getName()); }

        return VaadinUtils.createLinkButton(item.getName(), e -> {
            eventMgr.setEvent(event);
            eventMgr.setCategory(category);
            eventMgr.setItem(item);

            // todo - why do this instead of initializing ItemPage in ui?  it is not specific to item?
            ui.getNavigator().addView(ItemPage.NAME, new ItemPage());
            ui.getNavigator().navigateTo(ItemPage.NAME);
        });
    }

    private Date getEndDate(UserBid bid)
    {
        CatalogItem item = itemIdToItem.get(bid.getItemId());
        return item.getAvailabilityEnd();
    }

    private String getStatus(UserBid bid)
    {
        CatalogItem item = itemIdToItem.get(bid.getItemId());

        if (bid.getIsHighBid()) { return item.isSold() ? "Winning Bid" : "High Bid"; }
        else return "Outbid";
    }
}