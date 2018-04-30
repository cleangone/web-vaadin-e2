package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.vaadin.navigator.View;
import com.vaadin.ui.Button;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.bid.BidUtils;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createTextButton;

public class BidHandler implements View
{
    private final CatalogView catalogView;
    private final SessionManager sessionMgr;
    private final OrgManager orgMgr;
    private final UserManager userMgr;
    private final BidManager bidManager;
    private final MessageDisplayer messageDisplayer;

    private final User user;

    public BidHandler(CatalogView catalogView, SessionManager sessionMgr, MessageDisplayer messageDisplayer)
    {
        this.catalogView = catalogView;
        this.sessionMgr = sessionMgr;
        this.messageDisplayer = messageDisplayer;

        orgMgr = sessionMgr.getOrgManager();
        userMgr = sessionMgr.getUserManager();
        bidManager = orgMgr.getBidManager();
        user = userMgr.getUser();
    }

    public Button getQuickBidButton(CatalogItem item, OrgEvent event)
    {
        BigDecimal bidAmount = BidUtils.getIncrementedAmount(item.getPrice());
        return createTextButton("Bid $" + bidAmount, ev -> handleBid(item, event, bidAmount));
    }

    public void handleWatch(CatalogItem item, boolean watch)
    {
        if (watch) { user.addWatchedItemId(item.getId()); }
        else  { user.removeWatchedItemId(item.getId()); }

        userMgr.saveUser();
        catalogView.setCatalogLayout();
    }

    public void handleBid(CatalogItem item, OrgEvent event, BigDecimal maxBid)
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
                messageDisplayer.displayMessage("Bid submitted");

                if (bidStatus.getPreviousHighBid() != null)
                {
                    catalogView.schedule(new OutbidEmailSender(bidStatus.getPreviousHighBid(), sessionMgr));
                }

                catalogView.setCatalogLayout();
            }
        }

        Broadcaster.broadcast(item);
    }
}