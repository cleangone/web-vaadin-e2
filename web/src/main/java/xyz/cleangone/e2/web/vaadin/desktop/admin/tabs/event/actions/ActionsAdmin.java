package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.actions;


import com.vaadin.shared.ui.MarginInfo;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.manager.ActionManager;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.e2.web.manager.EntityChangeManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.BaseEventAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.EventsAdminLayout;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import static java.util.Objects.requireNonNull;


public abstract class ActionsAdmin extends BaseEventAdmin
{
    protected EntityChangeManager changeManager = new EntityChangeManager();

    protected EventManager eventMgr;
    protected ActionManager actionMgr;


    public ActionsAdmin(EventsAdminLayout eventsAdmin, MessageDisplayer msgDisplayer)
    {
        super(eventsAdmin, msgDisplayer);

        setSizeFull();
        setMargin(new MarginInfo(true, false, false, false)); // T/R/B/L
        setSpacing(true);
        setWidth("100%");
    }

    public void set(SessionManager sessionMgr)
    {
        eventMgr = sessionMgr.getEventManager();
        actionMgr = sessionMgr.getOrgManager().getActionManager();
    }

    public boolean unchanged(OrgEvent event)
    {
        if (changeManager.unchanged(event) &&
            changeManager.unchanged(event, EntityType.Action))
        {
            return true;
        }

        return false;
    }

}