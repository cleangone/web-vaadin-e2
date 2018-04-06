package xyz.cleangone.e2.web.vaadin.desktop.admin;

import com.vaadin.ui.TabSheet;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.event.EventsAdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;

import java.util.List;


// todo - do not put in a tabsheet


public class EventAdminPage extends BaseAdminPage
{
    public static final String NAME = "EventAdmin";
    public static final String DISPLAY_NAME = "Admin";

    private EventsAdminPage eventsAdmin = new EventsAdminPage(actionBar);
    private TabSheet.Tab eventsTab = tabsheet.addTab(createLayoutSizeFull(eventsAdmin), "Events");

    @Override
    protected PageDisplayType set(SessionManager sessionMgr)
    {
        super.set(sessionMgr);
        UserManager userMgr = sessionMgr.getUserManager();

        sessionMgr.resetEventManager();
        actionBar.set(sessionMgr);

        List<OrgTag> tags = orgMgr.getTagManager().getEventAdminRoleTags();
        boolean isEventAdmin = userMgr.userHasEventAdmin(orgMgr.getOrg(), tags);
        eventsTab.setEnabled(isEventAdmin);
        if (!isEventAdmin) { return PageDisplayType.NoRetrieval; }

        eventsAdmin.set(sessionMgr);
        return PageDisplayType.NoRetrieval;
    }
}
