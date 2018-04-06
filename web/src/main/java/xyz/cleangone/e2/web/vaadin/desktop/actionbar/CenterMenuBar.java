package xyz.cleangone.e2.web.vaadin.desktop.actionbar;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.MenuBar;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.CalendarPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.OrgPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;

import java.util.Date;
import java.util.List;

import static xyz.cleangone.e2.web.vaadin.util.PageUtils.*;

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
        if (org == null ) { return PageDisplayType.NotApplicable; }

        if (changeManager.unchanged(org) &&
            changeManager.unchanged(org, EntityType.Entity, EntityType.Event))
        {
            return PageDisplayType.NoChange;
        }

        changeManager.reset(org);
        removeItems();

        MenuBar.MenuItem homeItem = addItem("", null, getNavigateCmd(OrgPage.NAME));
        setMenuItem(homeItem, VaadinIcons.HOME, "Home");

        List<OrgEvent> events = eventMgr.getActiveEvents();
        if (!events.isEmpty())
        {
            MenuBar.MenuItem eventsItem = addItem("Events", null, null);
            for (OrgEvent event : events)
            {
                if (!event.getUseOrgBanner())
                {
                    eventsItem.addItem(event.getName(), null, new MenuBar.Command() {
                        public void menuSelected(MenuBar.MenuItem selectedItem) {
                            sessionMgr.navigateTo(event, getUI().getNavigator());
                        }
                    });
                }
            }
        }

        addNavigateItem(CalendarPage.NAME, VaadinIcons.CALENDAR, this);
        msgMenuItem = addItem(sessionMgr.getAndClearMsg(), null, null);

        return PageDisplayType.ObjectRetrieval;
    }

    public void displayMessage(String msg)
    {
        msgMenuItem.setText(msg);
    }

}
