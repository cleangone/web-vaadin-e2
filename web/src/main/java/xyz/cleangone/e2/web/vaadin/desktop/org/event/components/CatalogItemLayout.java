package xyz.cleangone.e2.web.vaadin.desktop.org.event.components;

import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.*;
import org.vaadin.kim.countdownclock.CountdownClock;
import xyz.cleangone.data.aws.dynamo.entity.bid.UserBid;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.ImageManager;
import xyz.cleangone.data.manager.event.BidManager;
import xyz.cleangone.e2.web.vaadin.desktop.MyUI;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageDimension;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageLabel;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventUtils;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.ItemPage;

import java.util.List;
import java.util.Map;

import static xyz.cleangone.e2.web.vaadin.desktop.org.event.EventUtils.*;

public class CatalogItemLayout extends VerticalLayout
{
    private static boolean COLORS = true;

    private final Integer relativeWidth;

    public CatalogItemLayout(CatalogItem item, User user, Button quickBidButton, EventManager eventMgr, BidManager bidManager, Map<String, OrgEvent> eventsById)
    {
        relativeWidth = item.getRelativeWidth();

        setMargin(false);
        setSpacing(false);
        setStyleName("category");
        setDefaultComponentAlignment(new Alignment(AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER));
        if (MyUI.COLORS) { addStyleName("backRed"); }

        List<S3Link> images = item.getImages();
        if (images != null && !images.isEmpty())
        {
            String imageUrl = ImageManager.getUrl(images.get(0));
            ImageLabel imageLabel = new ImageLabel(imageUrl, ImageDimension.height(250));
            addComponent(imageLabel);
        }

        addComponent(new Label(item.getName()));

        PriceLayout priceLayout = getPriceLayout(item, user, bidManager);
        addComponent(priceLayout);

        if (EventUtils.showCountdownClock(item.getAvailabilityEnd()))
        {
            CountdownClock clock = EventUtils.getCountdownClock(item.getAvailabilityEnd());
            addComponent(clock);
        }

        if (quickBidButton != null && priceLayout.userOutbid && user.getShowQuickBid()) { addComponent(quickBidButton); }

        addLayoutClickListener( e -> {
            if (eventsById != null) { eventMgr.setEvent(eventsById.get(item.getEventId())); }
            eventMgr.setItem(item);
            getUI().getNavigator().addView(ItemPage.NAME, new ItemPage());
            getUI().getNavigator().navigateTo(ItemPage.NAME);
        });
    }

    private PriceLayout getPriceLayout(CatalogItem item, User user, BidManager bidManager)
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

    public Integer getRelativeWidth()
    {
        return relativeWidth;
    }

    class PriceLayout extends HorizontalLayout
    {
        boolean userOutbid = false;
    }
}