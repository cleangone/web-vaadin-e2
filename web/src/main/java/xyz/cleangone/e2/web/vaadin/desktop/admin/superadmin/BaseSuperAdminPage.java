package xyz.cleangone.e2.web.vaadin.desktop.admin.superadmin;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.manager.VaadinSessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.BaseAdminPage;

public abstract class BaseSuperAdminPage extends BaseAdminPage
{
    protected VerticalLayout mainLayout = new VerticalLayout();

    public BaseSuperAdminPage()
    {
        mainLayout.setMargin(new MarginInfo(false, false, false, true)); // T/R/B/L margins
        mainLayout.setSpacing(true);

        pageLayout.addComponent(mainLayout);
        pageLayout.setExpandRatio(mainLayout, 1.0f);
    }

    public void enter(ViewChangeListener.ViewChangeEvent event)
    {
        set(VaadinSessionManager.getExpectedSessionManager());
    }

    protected void set(SessionManager sessionMgr)
    {
        super.set(sessionMgr);

        sessionMgr.resetOrg();
        actionBar.set(sessionMgr);
        set();
    }

    protected abstract void set();
}
