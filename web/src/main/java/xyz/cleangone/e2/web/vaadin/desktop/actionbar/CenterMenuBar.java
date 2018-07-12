package xyz.cleangone.e2.web.vaadin.desktop.actionbar;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.MenuBar;
import xyz.cleangone.data.aws.dynamo.entity.lastTouched.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.CalendarPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.OrgPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;

public class CenterMenuBar extends BaseMenuBar
{
    private MenuBar.MenuItem msgMenuItem;

    public PageDisplayType set(SessionManager sessionMgr)
    {
        super.set(sessionMgr);
        return set();
    }

    private PageDisplayType set()
    {
        Organization org = sessionMgr.getOrg();
        if (org == null )
        {
            removeItems();
            return PageDisplayType.NotApplicable;
        }

        if (changeManager.unchanged(org) &&
            changeManager.unchanged(org, EntityType.Entity, EntityType.Event))
        {
            return PageDisplayType.NoChange;
        }

        // todo - move cart to right of events (old calendar spot)


        changeManager.reset(org);
        removeItems();

        if (!sessionMgr.isMobileBrowser())
        {
            addIconOnlyItem("Home", VaadinIcons.HOME, getNavigateCmd(OrgPage.NAME));
        }

        if (!sessionMgr.isMobileBrowser())
        {
            String caption = org.getEventCaptionPlural() == null ? "Events" : org.getEventCaptionPlural();
            MenuBar.MenuItem eventsItem = addItem(caption, null, null);
            addEvents(eventsItem);

            addIconOnlyItem(CalendarPage.NAME, VaadinIcons.CALENDAR, getNavigateCmd(CalendarPage.NAME));
        }

        msgMenuItem = addItem(sessionMgr.getAndClearMsg(), null, null);

        return PageDisplayType.ObjectRetrieval;
    }

    public void displayMessage(String msg)
    {
        msgMenuItem.setText(msg);
    }

}
