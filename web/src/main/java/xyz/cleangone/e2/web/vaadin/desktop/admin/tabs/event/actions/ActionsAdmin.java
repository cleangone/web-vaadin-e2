package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.actions;

import com.vaadin.shared.ui.MarginInfo;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.action.ActionType;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.manager.ActionManager;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.e2.web.manager.EntityChangeManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.BaseEventAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.EventsAdminLayout;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;


public abstract class ActionsAdmin extends BaseEventAdmin
{
    protected EntityChangeManager changeManager = new EntityChangeManager();

    protected EventManager eventMgr;
    protected ActionManager actionMgr;


    public ActionsAdmin(EventsAdminLayout eventsAdmin, MessageDisplayer msgDisplayer)
    {
        super(eventsAdmin, msgDisplayer);
        setLayout(this, MARGIN_TR, SPACING_TRUE, SIZE_FULL, BACK_GREEN);
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