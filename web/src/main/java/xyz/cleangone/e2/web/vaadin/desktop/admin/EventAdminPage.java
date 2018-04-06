package xyz.cleangone.e2.web.vaadin.desktop.admin;

import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.event.EventsAdminLayout;


public class EventAdminPage extends BaseAdminPage
{
    public static final String NAME = "EventAdmin";
    public static final String DISPLAY_NAME = "Admin";

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
}
