package xyz.cleangone.e2.web.vaadin.desktop.admin;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.manager.VaadinSessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.ActionBar;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public abstract class BaseAdminPage extends Panel implements View
{
    protected VerticalLayout pageLayout = vertical(MARGIN_FALSE, SPACING_TRUE, SIZE_FULL, BACK_YELLOW);
    protected ActionBar actionBar = new ActionBar();

    protected SessionManager sessionMgr;
    protected OrgManager orgMgr;

    public BaseAdminPage()
    {
        // panel fills the browser screen
        setSizeFull();

        // pageLayout sits in components, scrolls if doesn't fit
        // todo - making pagelayout full - other will scroll in it
        pageLayout.addComponent(actionBar);
        setContent(pageLayout);
    }

    public static String getName() { return ""; }
    public static String getDisplayName() { return ""; }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event)
    {
        SessionManager sessionManager = VaadinSessionManager.getExpectedSessionManager();
        if (sessionManager.hasOrg())
        {
            set(sessionManager);
        }
    }

    protected void set(SessionManager sessionMgr)
    {
        this.sessionMgr = sessionMgr;
        orgMgr = sessionMgr.getOrgManager();
    }

    protected VerticalLayout createLayoutSizeFull(Component component)
    {
        return vertical(component, MARGIN_FALSE, SPACING_TRUE, SIZE_FULL, BACK_RED);
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
        return vertical(component, MARGIN_L, SPACING_TRUE);
    }

}
