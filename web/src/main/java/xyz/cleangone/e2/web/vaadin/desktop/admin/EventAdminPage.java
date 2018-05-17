package xyz.cleangone.e2.web.vaadin.desktop.admin;

import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.EventsAdminLayout;

public class EventAdminPage extends BaseAdminPage
{
    public static final String NAME = "EventAdmin";
    public static final String DISPLAY_NAME = "Event Admin";

    private EventsAdminLayout eventsAdmin = new EventsAdminLayout(actionBar);

    public EventAdminPage()
    {
        pageLayout.setSpacing(false);
        pageLayout.addComponent(eventsAdmin);
        pageLayout.setExpandRatio(eventsAdmin, 1.0f);
    }

    @Override
    protected void set(SessionManager sessionMgr)
    {
        sessionMgr.resetEventManager();
        actionBar.set(sessionMgr);
        eventsAdmin.set(sessionMgr);
    }

    public static String getName() { return NAME; }
    public static String getDisplayName() { return DISPLAY_NAME; }
}
