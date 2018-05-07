package xyz.cleangone.e2.web.vaadin.desktop.org.event.components;

import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.action.ActionType;
import xyz.cleangone.data.aws.dynamo.entity.item.CartItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.person.Person;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.aws.dynamo.entity.purchase.Cart;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.ActionBar;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.payment.PaymentPage;
import xyz.cleangone.e2.web.vaadin.util.PageUtils;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class FulfillPledgesPanel extends BaseActionPanel
{
    public FulfillPledgesPanel(SessionManager sessionMgr, ActionBar actionBar)
    {
        super(sessionMgr, actionBar);
        panelLayout.setMargin(true);
        set();
    }

    private void set()
    {
        if (!panelHasContent(event, user)) { return; }

        // look for pledges
        List<Action> actions = actionMgr.getActionsBySourcePerson(user.getId(), event.getId());
        List<Action> pledges = actions.stream()
            .filter(a -> a.getActionType() == ActionType.Pledged)
            .collect(Collectors.toList());
        if (pledges.isEmpty()) { return; }

        // look for pledges that have not been fulfilled
        Set<String> fulfilledPledgeIds = actions.stream()
            .filter(a -> a.getActionType() == ActionType.FulfilledPledge)
            .map(Action::getReferenceActionId)
            .collect(Collectors.toSet());

        List<Action> unfulfilledPledges = pledges.stream()
            .filter(a -> !fulfilledPledgeIds.contains(a.getId()))
            .collect(Collectors.toList());
        if (unfulfilledPledges.isEmpty()) { return; }

        // get pledges ready for fulfillment
        List<CartItem> cartItems = unfulfilledPledges.stream()
            .map(this::getFulfillPledgeCartItem)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        if (cartItems.isEmpty()) { return; }

        // populate components
        Button button = VaadinUtils.createTextButton("Fulfill Pledges", e -> {
            Cart cart = sessionMgr.getCart();
            cart.setReturnPage(EventPage.NAME);
            cart.addItems(cartItems);
            navigateTo(PaymentPage.NAME);
        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.addComponents(new Label("The Event has completed. Please fulfill your pledges"), button);
        panelLayout.addComponent(layout);
    }

    private CartItem getFulfillPledgeCartItem(Action pledge)
    {
        String personId = pledge.getTargetPersonId();
        if (personId == null) { return null; }  // weird - pledges require a target person

        EventParticipant participant = eventMgr.getEventParticipant(personId);
        if (participant == null || participant.getPerson() == null) { return null; } // weird - participant was removed

        if (participant.getCount() == 0) { return null; }  // participant iterations not set - cannot fulfill pledge

        // figure out how many iterations the target person did
        Person person = participant.getPerson();
        int iterations = participant.getCount();
        String iterationDesc = iterations + " " + event.getIterationLabel(iterations);
        BigDecimal amountDue = pledge.getIterationAmount().multiply(new BigDecimal(iterations));
        String fullmentActionDesc = pledge.getDescription() + ", " + iterationDesc;
        String cartItemName = "Fulfillment of " + pledge.getDescription() + " " + event.getName() + " pledge. " +
            person.getFirstLast() + " completed " + iterationDesc;

        return new CartItem(cartItemName, amountDue, event, participant, pledge, fullmentActionDesc)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ;
    }

    public static boolean panelHasContent(OrgEvent event, User user)
    {
        return (event.getEventCompleted() && event.getAcceptPledges() && user != null);
    }

    public boolean unfulfilledPledgesExist()
    {
        return panelLayout.getComponentCount() > 0;
    }

}
