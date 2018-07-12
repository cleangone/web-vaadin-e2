package xyz.cleangone.e2.web.vaadin.desktop.org.event.components;

import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.ShortcutAction;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.DateRenderer;
import org.vaadin.kim.countdownclock.CountdownClock;
import xyz.cleangone.data.aws.dynamo.entity.bid.ItemBid;
import xyz.cleangone.data.aws.dynamo.entity.bid.UserBid;
import xyz.cleangone.data.aws.dynamo.entity.item.CartItem;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.aws.dynamo.entity.purchase.Cart;
import xyz.cleangone.data.manager.ImageManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.data.manager.event.BidManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.ActionBar;
import xyz.cleangone.web.vaadin.disclosure.BaseDisclosure;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageDimension;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageLabel;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.BidHandler;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.CatalogPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventUtils;
import xyz.cleangone.e2.web.vaadin.desktop.org.profile.BidsPage;
import xyz.cleangone.web.vaadin.ui.DollarField;
import xyz.cleangone.web.vaadin.util.PageUtils;
import xyz.cleangone.web.vaadin.util.VaadinUtils;

import java.text.SimpleDateFormat;
import java.util.*;

import static xyz.cleangone.e2.web.vaadin.desktop.org.event.EventUtils.*;
import static xyz.cleangone.web.vaadin.util.VaadinUtils.*;

public class ItemLayout extends VerticalLayout
{
    public static final String NAME = "Item";

    private final BidHandler bidHandler;
    private final BidManager bidManager;
    private final User user;
    private final OrgTag category;
    private final boolean closeReturnsToWatch;

    private VerticalLayout bidBuyLayout = vertical(MARGIN_FALSE);

    public ItemLayout(
        CatalogItem item, OrgTag category, OrgEvent event, int pageHeight, BidHandler bidHandler, SessionManager sessionMgr, ActionBar actionBar, boolean closeReturnsToWatch)
    {
        this.bidHandler = bidHandler;
        bidManager = sessionMgr.getOrgManager().getBidManager();
        this.category = category;
        this.closeReturnsToWatch = closeReturnsToWatch;

        UserManager userMgr = sessionMgr.getUserManager();
        user = userMgr.getUser();

        setLayout(this, MARGIN_FALSE, SPACING_TRUE, BACK_GREEN);
        addLayoutClickListener(e -> {
            if (e.getMouseEventDetails().getButton() == MouseEventDetails.MouseButton.RIGHT) { closeItem(); }
        });

        Button closeButton = createCloseButton();
        closeButton.addClickListener(e -> closeItem());
        closeButton.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);

        AbstractOrderedLayout itemLayout = sessionMgr.isMobileBrowser() ? vertical(MARGIN_FALSE) : horizontal(MARGIN_FALSE);

        List<S3Link> images = item.getImages();
        if (images != null && !images.isEmpty())
        {
            String imageUrl = ImageManager.getUrl(images.get(0));
            PopupView fullImagePopup = getFullImagePopup(imageUrl,  pageHeight);

            ImageLabel imageLabel = new ImageLabel(imageUrl, ImageDimension.width(400));
            VerticalLayout imageLayout = vertical(imageLabel, MARGIN_FALSE);
            imageLayout.addLayoutClickListener(e -> {
                if (e.getMouseEventDetails().getButton() == MouseEventDetails.MouseButton.RIGHT) { closeItem(); }
                else { fullImagePopup.setPopupVisible(true); }
            });

            itemLayout.addComponents(imageLayout, fullImagePopup);
        }

        VerticalLayout detailslayout = vertical((sessionMgr.isMobileBrowser() ? MARGIN_TRUE : MARGIN_FALSE), WIDTH_100_PCT);
        itemLayout.addComponent(detailslayout);
        detailslayout.addComponent(VaadinUtils.createLabel(item.getName(), "title"));

        TagManager tagMgr = sessionMgr.getOrgManager().getTagManager();
        List<OrgTag> tags = tagMgr.getTags(item.getTagIds());
        for (OrgTag tag : tags)
        {
            detailslayout.addComponent(new Label(tag.getTagTypeName() + ": " + tag.getName()));
        }

        boolean userOutbid = false;
        String displayPrice = item.getDisplayPrice();
        if (item.isAuction())
        {
            if (item.isAvailable())
            {
                userOutbid = addBidDetails(detailslayout, displayPrice, item);
            }
            else if (item.isSold() || item.isUnsold())
            {
                detailslayout.addComponent(new Label(displayPrice));
                detailslayout.addComponent(new Label("Auction has ended"));
                if (item.isSold())
                {
                    if (userMgr.hasUser())
                    {
                        if (user.getId().equals(item.getHighBidderId()))
                        {
                            detailslayout.addComponent(getGoodNewsLabel("You are the winner"));
                        }
                        else
                        {
                            UserBid userBid = bidManager.getUserBid(user, item);
                            if (userBid != null)
                            {
                                detailslayout.addComponent(getCautionLabel("Your bid of " + userBid.getDisplayMaxAmount() + " was outbid"));
                            }
                        }

                        if (user.isWatching(item.getId())) { detailslayout.addComponent(getStopWatchingButton(item)); }
                    }
                    else
                    {
                        detailslayout.addComponent(getCautionLabel("Item is Sold"));
                    }
                }
            }
        }
        else if (item.isDrop())
        {
            if (item.isAvailable())
            {
                if (item.isInDropWindow()) { userOutbid = addBidDetails(detailslayout, displayPrice, item); }
                else { detailslayout.addComponent(new Label(displayPrice)); }
            }
            else if (item.isSold())
            {
                detailslayout.addComponent(new Label(displayPrice));
                detailslayout.addComponent(getSoldLabel());
            }
        }
        else if (item.isPurchase())
        {
            detailslayout.addComponent(new Label(displayPrice));

            if (item.isSold()) { detailslayout.addComponent(getSoldLabel()); }

            Integer quantity = item.getQuantity();
            if (quantity != null && quantity > 0) { detailslayout.addComponent(new Label(quantity + " remaining")); }
        }

        // bid or buy button
        if (item.isAvailable())
        {
            if (item.isAuction() ||
                (item.isDrop() && item.isInDropWindow()))
            {
                // check weird case where auc has ended but item not yet updated
                Date now = new Date();
                Date endDate = item.getAvailabilityEnd();
                if (endDate == null || endDate.after(now))
                {
                    if (userMgr.hasUser())
                    {
                        DollarField maxBidField = new DollarField(null, "$ Max Bid");
                        Button bidButton = createTextButton("Bid", ev -> bidHandler.handleBid(item, maxBidField.getDollarValue()));
                        addEnterKeyShortcut(bidButton, maxBidField);

                        HorizontalLayout bidLayout = new HorizontalLayout(maxBidField, bidButton);
                        bidLayout.setComponentAlignment(bidButton, Alignment.MIDDLE_LEFT);
                        bidBuyLayout.addComponent(bidLayout);

                        if (userOutbid && user.getShowQuickBid()) { bidBuyLayout.addComponent(bidHandler.getQuickBidButton(item)); }
                    }
                    else
                    {
                       bidBuyLayout.addComponent(new Label("Login to bid"));
                    }

                    detailslayout.addComponent(bidBuyLayout);
                }
            }
            else
            {
                // purchase, or drop item that is out of window and can be purchased
                detailslayout.addComponent(createTextButton("Purchase", ev ->
                {
                    // todo - has not been purchased yet, but may be in another cart - do you lock an item when you put it in the cart?

                    Cart cart = sessionMgr.getCart();
                    cart.addItem(new CartItem(item, event, category));
                    cart.setReturnPage(EventPage.NAME);

                    actionBar.setCartMenuItem();
                    actionBar.displayMessage("Item added to Cart");
                }));
            }
        }

        if (item.isAuction() || item.isDrop())
        {
            List<ItemBid> bids = bidManager.getItemBids(item);
            if (!bids.isEmpty()) { detailslayout.addComponent(new BidsDisclosure(bids)); }
        }

        addComponents(closeButton, itemLayout);
    }

    private PopupView getFullImagePopup(String imageUrl, int pageHeight)
    {
        VerticalLayout fullImageLayout = vertical(createImageLabel(imageUrl), MARGIN_TRUE);
        Panel panel = new Panel(fullImageLayout); // panel for scrollbars
        panel.setHeight(pageHeight + "px");
        panel.setStyleName(BACK_BLACK);

        PopupView popup = new PopupView(null, panel);
        fullImageLayout.addLayoutClickListener(e -> popup.setPopupVisible(false));

        return popup;
    }

    // return true is user is outbid (a bit of a hack)
    private boolean addBidDetails(VerticalLayout detailslayout, String displayPrice, CatalogItem item)
    {
        boolean userOutbid = false;
        ItemBid highBid = item.getHighBidId() == null ? null : bidManager.getItemBid(item.getHighBidId());

        String bidDesc = (highBid == null ? "Starting" : "Current");
        detailslayout.addComponent(new Label(bidDesc + " Bid: " + displayPrice));
        if (highBid != null && user != null)
        {
            if (highBid.getUserId().equals(user.getId()))
            {
                detailslayout.addComponent(getGoodNewsLabel("You are the high bidder, with a max bid of " + highBid.getDisplayMaxAmount()));
            }
            else
            {
                UserBid userBid = bidManager.getUserBid(user, item);
                if (userBid != null)
                {
                    detailslayout.addComponent(getCautionLabel("Your bid of " + userBid.getDisplayMaxAmount() + " was outbid"));
                    userOutbid = true;
                }
            }
        }

        if (user != null)
        {
            detailslayout.addComponent(user.isWatching(item.getId()) ? getStopWatchingButton(item) :
                createTextButton("Watch", ev -> bidHandler.handleWatch(item, true)));
        }

        Date endDate = item.getAvailabilityEnd();
        if (endDate != null)
        {
            String auctionDesc = item.isAuction() ? "Auction" : "Drop Auction";  // todo - should verify drop

            Date now = new Date();
            if (endDate.before(now))
            {
                // auc ended but item status not yet updated
                detailslayout.addComponent(new Label(auctionDesc + " has ended"));
            }
            else if (EventUtils.showCountdownClock(endDate))
            {
                CountdownClock clock = EventUtils.getCountdownClock(endDate);
                clock.addEndEventListener(e -> detailslayout.removeComponent(bidBuyLayout));
                clock.setStyleName("fontBold");

                detailslayout.addComponents(new HorizontalLayout(new Label(auctionDesc + " ends in"), clock));
            }
            else
            {
                SimpleDateFormat sdf = (endDate.getTime() - FIVE_DAYS < (new Date()).getTime()) ? PageUtils.SDF_THIS_WEEK : PageUtils.SDF_NEXT_WEEK;
                detailslayout.addComponent(new Label(auctionDesc + " ends " + sdf.format(endDate)));
            }
        }

        return userOutbid;
    }

    private Button getStopWatchingButton(CatalogItem item)
    {
        return createTextButton("Stop Watching", ev -> bidHandler.handleWatch(item, false));
    }

    private void closeItem()
    {
        if (closeReturnsToWatch)
        {
            getUI().getNavigator().navigateTo(BidsPage.WATCH_NAME);
        }
        else
        {
            String viewName = CatalogPage.NAME + "-" + category.getName();
            getUI().getNavigator().addView(viewName, new CatalogPage());
            getUI().getNavigator().navigateTo(viewName);
        }
    }

    class BidsDisclosure extends BaseDisclosure
    {
        List<FormattedBid> formattedBids = new ArrayList<>();

        BidsDisclosure(List<ItemBid> bids)
        {
            super("", new VerticalLayout());
            setWidth("100%");

            Map<String, String> userIdToAnonName = new HashMap<>();
            for (ItemBid bid : bids)
            {
                if (!userIdToAnonName.containsKey(bid.getUserId()))
                {
                    String anonName = (user != null && bid.getUserId().equals(user.getId())) ? "You" : "Bidder " + (userIdToAnonName.size() + 1);
                    userIdToAnonName.put(bid.getUserId(), anonName);
                }

                // formattedBids is sorted latest first
                formattedBids.add(0, new FormattedBid(userIdToAnonName.get(bid.getUserId()), "$" + bid.getCurrAmount(), bid.getCreatedDate()));
            }

            Grid<FormattedBid> grid = new Grid<>();
            grid.setWidth("100%");
            grid.addColumn(FormattedBid::getBidder);
            grid.addColumn(FormattedBid::getAmount);
            grid.addColumn(FormattedBid::getDate).setRenderer(new DateRenderer(PageUtils.SDF_NEXT_WEEK));

            grid.removeHeaderRow(0);
            grid.setHeightByRows(formattedBids.size() == 0 ? 1 : formattedBids.size());
            grid.setDataProvider(new ListDataProvider<>(formattedBids));

            setDisclosureCaption();
            mainLayout.addComponents(grid);
        }

        public void setDisclosureCaption()
        {
             setDisclosureCaption(formattedBids.size() + " " + (formattedBids.size() == 1 ? "Bid" : "Bids"));
        }

        class FormattedBid
        {
            String bidder;
            String amount;
            Date date;

            FormattedBid(String bidder, String amount, Date date)
            {
                this.bidder = bidder;
                this.amount = amount;
                this.date = date;
            }

            String getBidder()
            {
                return bidder;
            }
            String getAmount()
            {
                return amount;
            }
            Date getDate()
            {
                return date;
            }
        }
    }
}