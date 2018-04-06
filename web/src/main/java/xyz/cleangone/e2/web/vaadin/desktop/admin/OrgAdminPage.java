package xyz.cleangone.e2.web.vaadin.desktop.admin;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.event.EventsAdminLayout;
import xyz.cleangone.e2.web.vaadin.desktop.admin.org.OrgAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.PeopleAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.StatsAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.TagsAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.UsersAdmin;


public class OrgAdminPage extends BaseAdminPage
{
    public static final String NAME = "OrgAdmin";
    public static final String DISPLAY_NAME = "Admin";

    private OrgAdmin orgAdmin = new OrgAdmin(actionBar);
    private StatsAdmin statsAdmin = new StatsAdmin();
    private TagsAdmin tagsAdmin = new TagsAdmin(actionBar, OrgTag.TagType.PersonTag);
    private PeopleAdmin peopleAdmin = new PeopleAdmin(actionBar);
    private UsersAdmin usersAdmin = new UsersAdmin(actionBar);
    private TagsAdmin categoriesAdmin = new TagsAdmin(actionBar, OrgTag.TagType.Category);
    private EventsAdminLayout eventsAdmin = new EventsAdminLayout(actionBar);

    private TabSheet tabsheet = new TabSheet();
    private TabSheet.Tab orgTab = tabsheet.addTab(createLayoutSizeFull(orgAdmin), "Organization");
    private TabSheet.Tab statsTab = tabsheet.addTab(createLayoutSizeFull(statsAdmin), "Stats");
    private TabSheet.Tab tagsTab = tabsheet.addTab(createLayout100Pct(tagsAdmin), "Tags");
    private TabSheet.Tab peopleTab = tabsheet.addTab(createLayoutSizeFull(peopleAdmin), "People");
    private TabSheet.Tab usersTab = tabsheet.addTab(createLayoutSizeFull(usersAdmin), "Users");
    private TabSheet.Tab categoriesTab = tabsheet.addTab(createLayout100Pct(categoriesAdmin), "Categories");
    private TabSheet.Tab eventsTab = tabsheet.addTab(createLayoutSizeFull(eventsAdmin), "Events");
    private TabSheet.Tab[] tabs = { orgTab, statsTab, tagsTab, peopleTab, usersTab, categoriesTab, eventsTab};

    public OrgAdminPage()
    {
        tabsheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabsheet.setHeight("100%");
        tabsheet.addSelectedTabChangeListener(e -> handleTabChangeEvent());

        pageLayout.addComponent(tabsheet);
        pageLayout.setExpandRatio(tabsheet, 1.0f);
    }

    protected void set(SessionManager sessionMgr)
    {
        super.set(sessionMgr);

        sessionMgr.resetEventManager();
        actionBar.set(sessionMgr);

        boolean isAdmin = sessionMgr.getUserManager().userIsAdmin(orgMgr.getOrg());
        for (TabSheet.Tab tab : tabs)
        {
            tab.setEnabled(isAdmin);
        }

        if (isAdmin)
        {
            // org is the initial tab
            orgAdmin.set(sessionMgr);
            orgTab.setCaption(sessionMgr.getOrgName());
        }
    }

    protected void handleTabChangeEvent()
    {
        TabSheet.Tab selectedTab = tabsheet.getTab(tabsheet.getSelectedTab());

        if (selectedTab == tagsTab) { tagsAdmin.set(orgMgr.getTagManager(), sessionMgr.getResetEventManager()); }
        else if (selectedTab == statsTab) { statsAdmin.set(orgMgr); }
        else if (selectedTab == categoriesTab) { categoriesAdmin.set(orgMgr.getTagManager(), sessionMgr.getResetEventManager()); }
        else if (selectedTab == peopleTab) { peopleAdmin.set(orgMgr); }
        else if (selectedTab == usersTab)  { usersAdmin.set(orgMgr, sessionMgr.getUserManager()); }
        else if (selectedTab == eventsTab)
        {
            sessionMgr.resetEventManager();
            eventsAdmin.set(sessionMgr);
        }
    }
}
