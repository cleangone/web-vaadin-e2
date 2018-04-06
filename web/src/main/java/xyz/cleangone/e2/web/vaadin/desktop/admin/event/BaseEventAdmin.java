package xyz.cleangone.e2.web.vaadin.desktop.admin.event;

import xyz.cleangone.e2.web.vaadin.desktop.admin.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

public abstract class BaseEventAdmin extends BaseAdmin
{
    protected final EventsAdminLayout eventsAdminLayout;

    public BaseEventAdmin(EventsAdminLayout eventsAdminLayout, MessageDisplayer msgDisplayer)
    {
        super(msgDisplayer);
        this.eventsAdminLayout = eventsAdminLayout;
    }

}