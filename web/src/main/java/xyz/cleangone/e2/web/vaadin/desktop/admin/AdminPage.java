package xyz.cleangone.e2.web.vaadin.desktop.admin;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.manager.VaadinSessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.event.EventsAdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.admin.org.OrgAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.banner.ActionBar;

import java.util.logging.Logger;


public class AdminPage extends Panel implements View
{
    private static final Logger LOG = Logger.getLogger(AdminPage.class.getName());
    public static final String NAME = "Admin";

    private VerticalLayout pageLayout = new VerticalLayout();
    private ActionBar actionBar = new ActionBar();
    private TabSheet tabsheet = new TabSheet();

    private OrgAdmin orgAdmin = new OrgAdmin(actionBar);
    private TagsAdmin tagsAdmin = new TagsAdmin(actionBar, OrgTag.TagType.PersonTag);
    private PeopleAdmin peopleAdmin = new PeopleAdmin(actionBar);
    private UsersAdmin usersAdmin = new UsersAdmin(actionBar);
    private TagsAdmin categoriesAdmin = new TagsAdmin(actionBar, OrgTag.TagType.Category);
    private EventsAdminPage eventsAdmin = new EventsAdminPage(actionBar);

    public AdminPage()
    {
        // components fills the browser screen
        setSizeFull();

        // pageLayout sits in components, scrolls if doesn't fit
        pageLayout.setMargin(false);
        pageLayout.setSpacing(true);
        pageLayout.setHeight("100%");
        pageLayout.setWidth("100%");
        setContent(pageLayout);

        tabsheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabsheet.setHeight("100%");

        pageLayout.addComponents(actionBar, tabsheet);
        pageLayout.setExpandRatio(tabsheet, 1.0f);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event)
    {
        UI ui = getUI(); // set

        SessionManager sessionMgr = VaadinSessionManager.getExpectedSessionManager();
        OrgManager orgMgr = sessionMgr.getOrgManager();
        sessionMgr.resetEventManager();
        actionBar.reset(sessionMgr);

        tabsheet.removeAllComponents();
        boolean admin = sessionMgr.getUserManager().userIsAdmin(orgMgr.getOrg());
        TabSheet.Tab orgTab = admin ? tabsheet.addTab(createLayoutSizeFull(orgAdmin), "Organization") : null;
        TabSheet.Tab tagsTab = admin ? tabsheet.addTab(createLayout100Pct(tagsAdmin), "Tags") : null;
        TabSheet.Tab peopleTab = admin ? tabsheet.addTab(createLayoutSizeFull(peopleAdmin), "People") : null;
        TabSheet.Tab usersTab = admin ? tabsheet.addTab(createLayoutSizeFull(usersAdmin), "Users") : null;
        TabSheet.Tab categoriesTab = admin ? tabsheet.addTab(createLayout100Pct(categoriesAdmin), "Categories") : null;
        TabSheet.Tab eventsTab = tabsheet.addTab(createLayoutSizeFull(eventsAdmin), "Events");

        if (admin)
        {
            // org is the initial tab
            orgAdmin.set(sessionMgr);
            orgTab.setCaption(sessionMgr.getOrgName());
        }
        else
        {
            eventsAdmin.set(sessionMgr);
        }

        // TODO - is there a better way than fetching this while the user waits?
        tabsheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                SessionManager sessionMgr = VaadinSessionManager.getExpectedSessionManager();

                Component selection = tabsheet.getSelectedTab();
                TabSheet.Tab selectedTab = tabsheet.getTab(selection);
                if (selectedTab == tagsTab) { tagsAdmin.set(orgMgr.getTagManager(), sessionMgr.getResetEventManager()); }
                else if (selectedTab == categoriesTab) { categoriesAdmin.set(orgMgr.getTagManager(), sessionMgr.getResetEventManager()); }
                else if (selectedTab == peopleTab) { peopleAdmin.set(orgMgr); }
                else if (selectedTab == usersTab)  { usersAdmin.set(orgMgr, sessionMgr.getUserManager()); }
                else if (selectedTab == eventsTab)
                {
                    sessionMgr.resetEventManager();
                    eventsAdmin.set(sessionMgr);
                }
            }
        });
    }

    private VerticalLayout createLayoutSizeFull(Component component)
    {
        VerticalLayout layout = createLayout(component);
        layout.setSizeFull();

        return layout;
    }

    private VerticalLayout createLayout100Pct(Component component)
    {
        VerticalLayout layout = createLayout(component);
        layout.setHeight("100%");
        layout.setWidth("100%");

        return layout;
    }

    private VerticalLayout createLayout(Component component)
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(new MarginInfo(false, true, false, true));  // T/R/B/L margins

        layout.setSpacing(true);
        layout.addComponent(component);

        return layout;
    }

}
