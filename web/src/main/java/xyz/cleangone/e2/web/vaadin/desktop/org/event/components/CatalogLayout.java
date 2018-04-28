package xyz.cleangone.e2.web.vaadin.desktop.org.event.components;

import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.*;
import org.vaadin.kim.countdownclock.CountdownClock;
import xyz.cleangone.data.aws.dynamo.entity.bid.UserBid;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.ImageManager;
import xyz.cleangone.data.manager.event.BidManager;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageDimension;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageLabel;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventUtils;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.ItemPage;

import java.util.List;

import static xyz.cleangone.e2.web.vaadin.desktop.org.event.EventUtils.*;

public class CatalogLayout extends GridLayout
{
    private static final int ITEM_COLS = 2;  // todo - make responsive to page size

    private final EventManager eventMgr;
    private final BidManager bidManager;
    private final ImageManager imageMgr;

    public CatalogLayout(int numItems, EventManager eventMgr, BidManager bidManager, ImageManager imageMgr)
    {
        this.eventMgr = eventMgr;
        this.bidManager = bidManager;
        this.imageMgr = imageMgr;

        int rows = Math.max((numItems + 1)/ITEM_COLS, 1);
        setColumns(ITEM_COLS);
        setRows(rows);
        setMargin(false);
    }

    public CatalogLayout(List<CatalogItem> items, User user, EventManager eventMgr, BidManager bidManager, ImageManager imageMgr)
    {
        this(items.size(), eventMgr, bidManager, imageMgr);

        for (CatalogItem item : items)
        {
            addItem(item, user, null);
        }
    }

    public void addItem(CatalogItem item, User user, Button quickBidButton)
    {
        addComponent(getItemLayout(item, user, quickBidButton));
    }

    private Component getItemLayout(CatalogItem item, User user, Button quickBidButton)
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(false);
        layout.setStyleName("category");
        layout.setDefaultComponentAlignment(new Alignment(AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER));

        List<S3Link> images = item.getImages();
        if (images != null && !images.isEmpty())
        {
            String imageUrl = imageMgr.getUrl(images.get(0));
            ImageLabel imageLabel = new ImageLabel(imageUrl, ImageDimension.height(250));
            layout.addComponent(imageLabel);
        }

        layout.addComponent(new Label(item.getName()));

        PriceLayout priceLayout = getPriceLayout(item, user);
        layout.addComponent(priceLayout);

        if (EventUtils.showCountdownClock(item.getAvailabilityEnd()))
        {
            CountdownClock clock = EventUtils.getCountdownClock(item.getAvailabilityEnd());
            layout.addComponent(clock);
        }

        if (quickBidButton != null && priceLayout.userOutbid && user.getShowQuickBid()) { layout.addComponent(quickBidButton); }

        layout.addLayoutClickListener( e -> {
            eventMgr.setItem(item);
            getUI().getNavigator().addView(ItemPage.NAME, new ItemPage());
            getUI().getNavigator().navigateTo(ItemPage.NAME);
        });

        return layout;
    }

    private PriceLayout getPriceLayout(CatalogItem item, User user)
    {
        PriceLayout layout = new PriceLayout();
        layout.setMargin(false);
        layout.setSizeUndefined();

        layout.addComponent(new Label(item.getDisplayPrice()));

        if (item.isSold())
        {
            layout.addComponent((user != null && user.getId().equals(item.getHighBidderId())) ?
                getGoodNewsLabel("Won") : getSoldLabel());
        }
        else
        {
            if (item.getHighBidderId() != null && user != null)
            {
                if (item.getHighBidderId().equals(user.getId()))
                {
                    layout.addComponent(getGoodNewsLabel("Winning"));
                }
                else
                {
                    UserBid userBid = bidManager.getUserBid(user, item);
                    if (userBid != null)
                    {
                        layout.addComponent(getCautionLabel("Outbid"));
                        layout.userOutbid = true;
                    }
                }
            }
        }

        return layout;
    }


    class PriceLayout extends HorizontalLayout
    {
        boolean userOutbid = false;
    }
}