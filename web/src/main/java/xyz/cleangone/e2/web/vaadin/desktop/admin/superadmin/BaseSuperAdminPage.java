package xyz.cleangone.e2.web.vaadin.desktop.admin.superadmin;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.manager.VaadinSessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.BaseAdminPage;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public abstract class BaseSuperAdminPage extends BaseAdminPage
{
    protected VerticalLayout mainLayout = vertical(MARGIN_L, SPACING_TRUE);

    public BaseSuperAdminPage()
    {
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
