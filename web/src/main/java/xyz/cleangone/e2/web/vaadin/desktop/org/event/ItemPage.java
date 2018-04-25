package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.DateRenderer;
import org.vaadin.kim.countdownclock.CountdownClock;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.bid.ItemBid;
import xyz.cleangone.data.aws.dynamo.entity.bid.UserBid;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.item.CartItem;
import xyz.cleangone.data.aws.dynamo.entity.purchase.Cart;
import xyz.cleangone.data.manager.ActionManager;
import xyz.cleangone.data.manager.event.BidManager;
import xyz.cleangone.data.manager.event.BidStatus;
import xyz.cleangone.e2.web.manager.OutbidEmailSender;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.disclosure.BaseDisclosure;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageDimension;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageLabel;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.util.DollarField;
import xyz.cleangone.e2.web.vaadin.util.PageUtils;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class ItemPage extends CatalogPage implements View
{
    public static final String NAME = "Item";
    private static long FIVE_DAYS = 1000 * 60 * 60 * 24 * 5;
    private BidManager bidManager;

    protected PageDisplayType set()
    {
        imageMgr = itemMgr.getImageManager();
        bidManager = orgMgr.getBidManager();

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
        detailslayout.setWidth("100%");
        layout.addComponent(detailslayout);

        detailslayout.addComponent(new Label(item.getName()));

        String displayPrice = item.getDisplayPrice();
        if (item.isAuction())
        {
            if (item.isAvailable())
            {
                addBidDetails(detailslayout, highBid, displayPrice, item, "Auction");
            }
            else if (item.isSold())
            {
                detailslayout.addComponent(new Label(displayPrice));
                detailslayout.addComponent(new Label("Auction has ended"));
            }
        }
        else if (item.isDrop())
        {
            if (item.isAvailable())
            {
                if (item.isInDropWindow()) { addBidDetails(detailslayout, highBid, displayPrice, item, "Drop Auction"); }
                else
                {
                    detailslayout.addComponent(new Label(displayPrice));

//                    Date date = new Date();
//                    date.setTime(date.getTime() + 1000 * 75);
//
//                    CountdownClock clock = new CountdownClock();
//                    clock.setDate(date);
//                    clock.setFormat("%m min %s sec");
//
//                    // countdown to a min left offscreen
//                    Date minDate = new Date(date.getTime() - 1000 * 60);  // finishes with one minute left
//                    CountdownClock switchClock = new CountdownClock();
//                    switchClock.setDate(minDate);
//                    switchClock.setFormat("Switch in %m min %s sec");
////                    switchClock.setVisible(false);
//                    switchClock.addEndEventListener( e -> {
//                        clock.setDate(date);
//                        clock.setFormat("%s sec");
//                    });
//
//                    detailslayout.addComponents(clock, switchClock);
                }
            }
            else if (item.isSold())
            {
                detailslayout.addComponent(new Label(displayPrice));
                detailslayout.addComponent(EventUtils.getSoldLabel());
            }
        }
        else if (item.isPurchase())
        {
            detailslayout.addComponent(new Label(displayPrice));

            if (item.isSold()) { detailslayout.addComponent(EventUtils.getSoldLabel()); }

            Integer quantity = item.getQuantity();
            if (quantity != null && quantity > 0) { detailslayout.addComponent(new Label(quantity + " remaining")); }
        }

        // bid or buy button
        if (item.isAvailable())
        {
            if (item.isAuction() ||
                (item.isDrop() && item.isInDropWindow()))
            {
                DollarField maxBidField = new DollarField("Max Bid");
                detailslayout.addComponent(maxBidField);
                detailslayout.addComponent(VaadinUtils.createTextButton("Bid", ev -> handleBid(item, maxBidField)));
            }
            else
            {
                // purchase, or drop item that is out of window and can be purchased
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

        if (item.isAuction() || item.isDrop())
        {
            List<ItemBid> bids = bidManager.getItemBids(item);
            if (!bids.isEmpty()) { detailslayout.addComponent(new BidsDisclosure(bids)); }
        }

        return layout;
    }

    private void addBidDetails(VerticalLayout detailslayout, ItemBid highBid, String displayPrice, CatalogItem item, String auctionDesc)
    {
        String bidDesc = (highBid == null ? "Starting" : "Current");
        detailslayout.addComponent(new Label(bidDesc + " Bid: " + displayPrice));
        if (highBid != null && user != null)
        {
            if (highBid.getUserId().equals(user.getId()))
            {
                detailslayout.addComponent(new Label("You are the high bidder, with a max bid of " + highBid.getDisplayMaxAmount()));
            }
            else
            {
                UserBid userBid = bidManager.getUserBid(user, item);
                if (userBid != null)
                {
                    detailslayout.addComponent(new Label("Your bid of " + userBid.getDisplayMaxAmount() + " was outbid"));
                }
            }
        }

        Date endDate = item.getAvailabilityEnd();
        if (endDate != null)
        {
            SimpleDateFormat sdf = (endDate.getTime() - FIVE_DAYS < (new Date()).getTime()) ? PageUtils.SDF_THIS_WEEK : PageUtils.SDF_NEXT_WEEK;
            detailslayout.addComponent(new Label(auctionDesc + " ends " + sdf.format(endDate)));
        }
    }

    private void handleBid(CatalogItem item, DollarField maxBidField)
    {
        BigDecimal maxBid = maxBidField.getDollarValue();
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