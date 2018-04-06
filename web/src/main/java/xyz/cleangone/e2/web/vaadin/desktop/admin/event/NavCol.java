package xyz.cleangone.e2.web.vaadin.desktop.admin.event;

import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NavCol extends VerticalLayout
{
    private static String STYLE_ADMIN_NAV = "adminNav";
    private static String STYLE_LINK = "link";
    private static String STYLE_LINK_ACTIVE = "linkActive";
    private static String STYLE_FONT_BOLD = "fontBold";
    private static String STYLE_INDENT = "marginLeft";

    protected final EventsAdminLayout eventsAdminLayout;

    protected SessionManager sessionMgr;
    protected EventManager eventMgr;
    protected UserManager userMgr;
    protected EventAdminPageType currPageType;

    public NavCol(EventsAdminLayout eventsAdminLayout)
    {
        this.eventsAdminLayout = eventsAdminLayout;

        setMargin(true);
        setSpacing(true);
        setWidthUndefined();
        setHeight("100%");
        setStyleName(STYLE_ADMIN_NAV);
    }

    public void set(SessionManager sessionMgr)
    {
        this.sessionMgr = sessionMgr;
        eventMgr = sessionMgr.getEventManager();
        userMgr = sessionMgr.getUserManager();
    }

    protected void set()
    {
        removeAllComponents();
        addEventLinks();
    }

    protected void addEventLinks()
    {
        OrgEvent currEvent = eventMgr.getEvent();

        String allEventsStyle = currEvent == null ? STYLE_LINK_ACTIVE : STYLE_LINK;
        addComponent(getLink(EventAdminPageType.EVENTS.toString(), allEventsStyle, e -> {
            sessionMgr.resetEventManager();
            setAdminPage(EventAdminPageType.EVENTS);
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

        if (!userMgr.userIsAdmin(sessionMgr.getOrg()))
        {
            // user is an event admin - determine which events to display
            TagManager tagMgr = sessionMgr.getOrgManager().getTagManager();
            Map<String, OrgTag> eventAdminRoleTagsById = tagMgr.getEventAdminRoleTagsById();
            Map<String, OrgEvent> eventsById = events.stream()
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

            for (String tagId : userMgr.getUser().getTagIds())
            {
                events.clear();
                if (eventAdminRoleTagsById.keySet().contains(tagId))
                {
                    OrgTag adminRoleTag = eventAdminRoleTagsById.get(tagId);
                    OrgEvent event = eventsById.get(adminRoleTag.getEventId());
                    if (event != null) { events.add(event); }
                }
            }
        }

        for (OrgEvent event : events)
        {
            longestNameLength = Math.max(longestNameLength, event.getName().length());
            if (event == currEvent)
            {
                VerticalLayout eventAdmin = new VerticalLayout();
                eventAdmin.setSpacing(false);
                eventAdmin.setMargin(false);
                eventAdmin.setStyleName(STYLE_INDENT);

                eventAdmin.addComponent(getLink(EventAdminPageType.GENERAL));
                eventAdmin.addComponent(getLink(EventAdminPageType.PARTICIPANTS));
                eventAdmin.addComponent(getLink(EventAdminPageType.TAGS));
                eventAdmin.addComponent(getLink(EventAdminPageType.ITEMS));
                eventAdmin.addComponent(getLink(EventAdminPageType.DATES));
                eventAdmin.addComponent(getLink(EventAdminPageType.ROLES));
                eventAdmin.addComponent(getLink(EventAdminPageType.USERS));
                eventAdmin.addComponent(getLink(EventAdminPageType.DONATIONS));
                eventAdmin.addComponent(getLink(EventAdminPageType.PURCHASES));

                VerticalLayout eventLayout = new VerticalLayout();
                eventLayout.setSpacing(false);
                eventLayout.setMargin(false);

                Label label = new Label(event.getName());
                label.setStyleName(STYLE_FONT_BOLD);
                eventLayout.addComponents(label, eventAdmin);
                addComponent(eventLayout);
            }
            else
            {
                addComponent(getLink(event.getName(), STYLE_LINK, e -> {
                    eventMgr.setEvent(event);
                    setAdminPage(EventAdminPageType.GENERAL);
                }));
            }
        }

        StringBuilder spacerName = new StringBuilder();
        for(int i=0; i < longestNameLength + 20; i++)
        {
            spacerName.append("&nbsp;");
        }
        Label spacer = VaadinUtils.getHtmlLabel(spacerName.toString());

        addComponent(spacer);
        setExpandRatio(spacer, 1.0f);
    }

    private Component getLink(EventAdminPageType pageType)
    {
        String styleName = currPageType == pageType ? STYLE_LINK_ACTIVE : STYLE_LINK;
        return getLink(pageType.toString(), styleName, e -> setAdminPage(pageType));
    }

    private void setAdminPage(EventAdminPageType pageType)
    {
        eventsAdminLayout.setAdminPage(pageType);
    }

    public void setAdminLinks(EventAdminPageType pageType)
    {
        currPageType = pageType;
        set();
    }

    private Component getLink(String text, String styleName, LayoutEvents.LayoutClickListener listener)
    {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setMargin(false);

        Label label = new Label(text);
        layout.addComponent(label);
        layout.addLayoutClickListener(listener);

        layout.setStyleName(styleName);

        return(layout);
    }
}
