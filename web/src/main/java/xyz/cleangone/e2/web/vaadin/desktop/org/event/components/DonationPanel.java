package xyz.cleangone.e2.web.vaadin.desktop.org.event.components;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.item.CartItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant;
import xyz.cleangone.data.aws.dynamo.entity.purchase.Cart;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.ActionBar;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventPage;
import xyz.cleangone.web.vaadin.ui.DollarField;

import java.math.BigDecimal;
import java.util.List;

import static xyz.cleangone.e2.web.vaadin.desktop.org.event.EventUtils.*;
import static xyz.cleangone.web.vaadin.util.VaadinUtils.*;

public class DonationPanel extends BaseActionPanel
{
    public DonationPanel(List<EventParticipant> participants, SessionManager sessionMgr, ActionBar actionBar)
    {
        super("Donation", sessionMgr, actionBar);

        panelLayout.setMargin(new MarginInfo(true, false, true, false)); // T/R/B/L margins
        panelLayout.addComponent(getPanelContent(participants));
    }

    private FormLayout getPanelContent(List<EventParticipant> participants)
    {
        DollarField amountField = new DollarField("Amount");
        ComboBox<EventParticipant> participantComboBox = createParticipantComboBox(participants);
        Button button = createTextButton("Donate", e -> handleDonation(amountField, participantComboBox));

        FormLayout layout = getFormLayout();
        if (!participants.isEmpty()) { layout.addComponents(participantComboBox); }
        layout.addComponents(amountField, button);

        return layout;
    }

    private void handleDonation(DollarField amountField, ComboBox<EventParticipant> participantComboBox)
    {
        BigDecimal amount = amountField.getDollarValue();
        if (amount == null || amount.equals(new BigDecimal(0))) { return; }

        EventParticipant participant = participantComboBox.getValue();
        String donationDescription = event.getName() + " donation " +
            (participant == null ? "" : " for " + participant.getFullName());

        Cart cart = sessionMgr.getCart();
        cart.addItem(new CartItem(donationDescription, amount, event, participant));
        cart.setReturnPage(EventPage.NAME);

        amountField.setValue("");
        participantComboBox.setValue(null);
        actionBar.setCartMenuItem();
        actionBar.displayMessage("Donation added to Cart");
    }
}
