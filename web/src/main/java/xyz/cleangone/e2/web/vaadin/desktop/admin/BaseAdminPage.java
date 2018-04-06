package xyz.cleangone.e2.web.vaadin.desktop.admin;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.manager.VaadinSessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.ActionBar;
import xyz.cleangone.e2.web.vaadin.desktop.admin.event.EventsAdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.admin.org.OrgAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.PeopleAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.StatsAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.TagsAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.UsersAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;

import java.util.Date;
import java.util.logging.Logger;

import static xyz.cleangone.e2.web.manager.PageStats.addRetrievalTime;


public abstract class BaseAdminPage extends Panel implements View
{
    protected VerticalLayout pageLayout = new VerticalLayout();
    protected ActionBar actionBar = new ActionBar();
    protected TabSheet tabsheet = new TabSheet();

    protected SessionManager sessionMgr;
    protected OrgManager orgMgr;

    public BaseAdminPage()
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
//        tabsheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
//            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) { handleTabChangeEvent(); }
//        });


        tabsheet.addSelectedTabChangeListener(e -> handleTabChangeEvent());



        pageLayout.addComponents(actionBar, tabsheet);
        pageLayout.setExpandRatio(tabsheet, 1.0f);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event)
    {
        Date start = new Date();
        SessionManager sessionManager = VaadinSessionManager.getExpectedSessionManager();
        if (sessionManager.hasOrg())
        {
            PageDisplayType pageDisplayType = set(sessionManager);
            addRetrievalTime(sessionManager.getOrg().getId(), getPageName(), pageDisplayType, start);
        }
    }

    protected PageDisplayType set(SessionManager sessionMgr)
    {
        this.sessionMgr = sessionMgr;
        orgMgr = sessionMgr.getOrgManager();

        return PageDisplayType.NotApplicable;
    }

    protected String getPageName()
    {
        return null;
    }


    protected void handleTabChangeEvent() {}

    protected VerticalLayout createLayoutSizeFull(Component component)
    {
        VerticalLayout layout = createLayout(component);
        layout.setSizeFull();

        return layout;
    }

    protected VerticalLayout createLayout100Pct(Component component)
    {
        VerticalLayout layout = createLayout(component);
        layout.setHeight("100%");
        layout.setWidth("100%");

        return layout;
    }

    protected VerticalLayout createLayout(Component component)
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(new MarginInfo(false, true, false, true));  // T/R/B/L margins

        layout.setSpacing(true);
        layout.addComponent(component);

        return layout;
    }

}
