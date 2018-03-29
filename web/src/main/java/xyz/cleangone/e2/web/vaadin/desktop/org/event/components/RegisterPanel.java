package xyz.cleangone.e2.web.vaadin.desktop.org.event.components;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import org.vaadin.dialogs.ConfirmDialog;
import xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.banner.ActionBar;
import xyz.cleangone.e2.web.vaadin.desktop.org.SigninPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventPage;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.List;
import java.util.stream.Collectors;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createTextButton;

public class RegisterPanel extends BaseActionPanel
{
    public RegisterPanel(List<OrgTag> eventTags, SessionManager sessionMgr, ActionBar actionBar)
    {
        super("Event Registration", sessionMgr, actionBar);

        panelLayout.setMargin(new MarginInfo(true, false, true, false)); // T/R/B/L margins
        setPanelContent(eventTags);
    }

    private void setPanelContent(List<OrgTag> eventTags)
    {
        if (user == null)
        {
            // user has to sign in to register
            setSignInContent();
            return;
        }

        List<OrgTag> userVisibleEventTags = eventTags.stream()
            .filter(OrgTag::getUserVisible)
            .collect(Collectors.toList());

        EventParticipant participant = eventMgr.getEventParticipant(user);
        if (participant == null)
        {
            // user can register
            setRegisterContent(userVisibleEventTags);
            return;
        }

        // user has already registered - allow them to change roles or unregister
        Button unregisterButton = getUnregisterButton(participant, userVisibleEventTags);
        if (userVisibleEventTags.isEmpty())
        {
            // user can only unregister
            panelLayout.addComponents(new Label("You are registered"), unregisterButton);
            return;
        }

        // user can change roles or unregister
        List<String> selectedTagIds = participant.getPerson().getTagIds();
        List<OrgTag> selectedUserVisibleEventTags = userVisibleEventTags.stream()
            .filter(tag -> selectedTagIds.contains(tag.getId()))
            .collect(Collectors.toList());

        CheckBoxGroup<OrgTag> tagCheckBoxGroup = new CheckBoxGroup<>("You are registered as", userVisibleEventTags);
        tagCheckBoxGroup.setItemCaptionGenerator(OrgTag::getName);
        selectedUserVisibleEventTags.forEach(tagCheckBoxGroup::select);

        Button changeRegistrationButton = getChangeRegistrationButton(participant, eventTags, tagCheckBoxGroup);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addComponents(changeRegistrationButton, unregisterButton);
        panelLayout.addComponents(tagCheckBoxGroup, buttonLayout);
    }

    private void setSignInContent()
    {
        panelLayout.addComponent(VaadinUtils.createLinkButton("Sign-in to register for Event", ev ->
        {
            sessionMgr.setNavToAfterLogin(EventPage.NAME);
            navigateTo(SigninPage.NAME);
        }));
    }

    private void setRegisterContent(List<OrgTag> eventTags)
    {
        CheckBoxGroup<OrgTag> tagCheckBoxGroup = new CheckBoxGroup<>("Register as", eventTags);
        tagCheckBoxGroup.setItemCaptionGenerator(OrgTag::getName);

        if (!eventTags.isEmpty())
        {
            panelLayout.addComponent(tagCheckBoxGroup);
        }

        panelLayout.addComponent(VaadinUtils.createTextButton("Register for Event", e ->
        {
            eventMgr.addEventParticipant(user, tagCheckBoxGroup.getSelectedItems());

            // todo - clean up fields

            actionBar.displayMessage("You have registered");
        }));
    }

    private Button getUnregisterButton(EventParticipant participant, List<OrgTag> eventTags)
    {
        return VaadinUtils.createTextButton("Unregister", e ->
        {
            ConfirmDialog.show(getUI(), "Confirm Unregister", "Unregister from  Event " + event.getName() + "?",
                "Unregister", "Cancel", new ConfirmDialog.Listener()
                {
                    public void onClose(ConfirmDialog dialog)
                    {
                        if (dialog.isConfirmed())
                        {
                            eventMgr.removeEventParticipant(participant, eventTags);

                            // todo - clean up fields

                            actionBar.displayMessage("You have unregistered");
                        }
                    }
                });
        });
    }

    private Button getChangeRegistrationButton(
        EventParticipant participant, List<OrgTag> eventTags, CheckBoxGroup<OrgTag> tagCheckBoxGroup)
    {
        return VaadinUtils.createTextButton("Update Registration", e ->
        {
            eventMgr.updateEventParticipant(participant, eventTags, tagCheckBoxGroup.getSelectedItems());

            // todo - clean up fields

            actionBar.displayMessage("Registration updated");
        });
    }
}