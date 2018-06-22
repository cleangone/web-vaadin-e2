package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event;

import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.BaseNavCol;
import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.AdminPageType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class NavCol extends BaseNavCol
{
    protected final EventsAdminLayout eventsAdminLayout;

    protected SessionManager sessionMgr;
    protected OrgManager orgMgr;
    protected EventManager eventMgr;
    protected UserManager userMgr;

    public NavCol(EventsAdminLayout eventsAdminLayout)
    {
        this.eventsAdminLayout = eventsAdminLayout;
    }

    public void set(SessionManager sessionMgr)
    {
        this.sessionMgr = sessionMgr;
        orgMgr = sessionMgr.getOrgManager();
        eventMgr = sessionMgr.getEventManager();
        userMgr = sessionMgr.getUserManager();
    }

    protected void addLinks()
    {
        OrgEvent currEvent = eventMgr.getEvent();

        String allEventsStyle = currEvent == null ? STYLE_LINK_ACTIVE : STYLE_LINK;

        String eventsCaptionPlural = orgMgr.getOrg().getEventCaptionPlural();
        String allEventsCaption = eventsCaptionPlural == null ? EventAdminPageType.EVENTS.toString() : "All " + eventsCaptionPlural;
        addComponent(getLink(allEventsCaption, allEventsStyle, e -> {
            sessionMgr.resetEventManager();
            setPage(EventAdminPageType.EVENTS);
        }));

        int longestNameLength = EventAdminPageType.EVENTS.toString().length();

        List<OrgEvent> events = new ArrayList<>(eventMgr.getEvents());

        // events may have changed because cache refreshed
        if (currEvent != null)
        {
            for (OrgEvent event : events)
            {
                if (event.getId().equals(currEvent.getId())) { currEvent = event; }
            }
        }

        User user = userMgr.getUser();
        String orgId = sessionMgr.getOrgId();
        if (!user.isOrgAdmin(orgId))
        {
            // user is an event admin - determine which events to display
            List<String> userAdminEventIds = user.getAdminPrivledgeEventIds(orgId);
            List<OrgEvent> allEvents = new ArrayList<>(events);

            events.clear();
            for (OrgEvent event : allEvents)
            {
                if (userAdminEventIds.contains(event.getId())) { events.add(event); }
            }
        }

        for (OrgEvent event : events)
        {
            longestNameLength = Math.max(longestNameLength, event.getName().length());
            if (event == currEvent)
            {
                VerticalLayout eventAdmin = vertical(MARGIN_FALSE, SPACING_FALSE);
                eventAdmin.setStyleName(STYLE_INDENT);

                eventAdmin.addComponent(getLink(EventAdminPageType.GENERAL));
                eventAdmin.addComponent(getLink(EventAdminPageType.PARTICIPANTS));
                eventAdmin.addComponent(getLink(EventAdminPageType.ITEMS));
                eventAdmin.addComponent(getLink(EventAdminPageType.DATES));
                eventAdmin.addComponent(getLink(EventAdminPageType.USERS));
                eventAdmin.addComponent(getLink(EventAdminPageType.DONATIONS));
                eventAdmin.addComponent(getLink(EventAdminPageType.PURCHASES));

                VerticalLayout eventLayout = vertical(MARGIN_FALSE, SPACING_FALSE);

                Label label = new Label(event.getName());
                label.setStyleName(STYLE_FONT_BOLD);
                eventLayout.addComponents(label, eventAdmin);
                addComponent(eventLayout);
            }
            else
            {
                addComponent(getLink(event.getName(), STYLE_LINK, e -> {
                    eventMgr.setEvent(event);
                    setPage(EventAdminPageType.GENERAL);
                }));
            }
        }

        addSpacer(longestNameLength);
    }

    protected void setPage(AdminPageType pageType)
    {
        eventsAdminLayout.setAdminPage(pageType);
    }
}
