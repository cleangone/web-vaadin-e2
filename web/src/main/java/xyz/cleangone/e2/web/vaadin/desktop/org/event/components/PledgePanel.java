package xyz.cleangone.e2.web.vaadin.desktop.org.event.components;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.ActionBar;
import xyz.cleangone.e2.web.vaadin.desktop.org.SigninPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventPage;
import xyz.cleangone.web.vaadin.ui.DollarField;
import xyz.cleangone.web.vaadin.util.VaadinUtils;

import java.math.BigDecimal;
import java.util.List;

import static xyz.cleangone.e2.web.vaadin.desktop.org.event.EventUtils.createParticipantComboBox;
import static xyz.cleangone.web.vaadin.util.VaadinUtils.createTextButton;

public class PledgePanel extends BaseActionPanel
{
    public PledgePanel(List<EventParticipant> participants, SessionManager sessionMgr, ActionBar actionBar)
    {
        super("Pledge", sessionMgr, actionBar);

        panelLayout.setMargin(new MarginInfo(true, false, true, false)); // T/R/B/L margins
        panelLayout.addComponent(getPanelContent(participants));
    }

    private Component getPanelContent(List<EventParticipant> participants)
    {
        if (user == null)
        {
            return VaadinUtils.createLinkButton("Sign-in to Pledge", e -> {
                sessionMgr.setNavToAfterLogin(EventPage.NAME);
                navigateTo(SigninPage.NAME);
            });
        }

        DollarField amountField = new DollarField("Amount" + getQualifier());
        ComboBox<EventParticipant> participantComboBox = createParticipantComboBox(participants);
        Button button = createTextButton("Pledge", e -> handlePledge(amountField, participantComboBox));

        FormLayout layout = getFormLayout();
        layout.addComponents(participantComboBox, amountField, button);

        return layout;
    }

    private String getQualifier() { return " per " + event.getIterationLabelSingular(); }

    private void handlePledge(DollarField amountField, ComboBox<EventParticipant> participantComboBox)
    {
        BigDecimal amount = amountField.getDollarValue();
        if (amount == null || amount.equals(new BigDecimal(0))) { return; }

        String desc = "$" + amount + getQualifier();

        // todo - participant is required

        EventParticipant participant = participantComboBox.getValue();
        Action pledge = actionMgr.createPledge(user, amount, desc, event, participant);
        actionMgr.save(pledge);

        amountField.setValue("");
        participantComboBox.setValue(null);
        actionBar.displayMessage("Pledge of " + desc + " made for " + participant.getFullName());
    }
}
