package xyz.cleangone.e2.web.vaadin.desktop.admin.superadmin;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.AdminPageType;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import java.util.HashMap;
import java.util.Map;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class SuperAdminLayout extends HorizontalLayout
{
    private final NavCol navCol;
    private final Map<AdminPageType, BaseAdmin> adminComponents = new HashMap<>();
    private final VerticalLayout mainLayout = vertical(MARGIN_FALSE, SIZE_FULL, BACK_RED);

    public SuperAdminLayout(MessageDisplayer msgDisplayer)
    {
        setLayout(this, MARGIN_FALSE, SPACING_TRUE, SIZE_FULL, BACK_BLUE);
        navCol = new NavCol(this);

        adminComponents.put(SuperAdminPageType.ORGS, new OrgsAdmin(msgDisplayer));
        adminComponents.put(SuperAdminPageType.USERS, new UsersAdmin(msgDisplayer));

        mainLayout.setHeight((UI.getCurrent().getPage().getBrowserWindowHeight() - 100) + "px");  // hack - like navCol

        addComponents(navCol, mainLayout);
        setExpandRatio(mainLayout, 1.0f);
    }

    public void set(SessionManager sessionMgr)
    {
        UI ui = getUI();
        for (BaseAdmin component : adminComponents.values())
        {
            component.set(sessionMgr, ui);
        }

        setAdminPage(SuperAdminPageType.ORGS);
    }

    public void setAdminPage(AdminPageType pageType)
    {
        navCol.setLinks(pageType);

        mainLayout.removeAllComponents();

        BaseAdmin component = adminComponents.get(pageType);
        component.set();
        mainLayout.addComponent(component);
    }
}