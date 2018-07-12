package xyz.cleangone.e2.web.vaadin.desktop.admin.superadmin;

import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.AdminPageType;
import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.BaseNavCol;

import static xyz.cleangone.web.vaadin.util.VaadinUtils.*;

public class NavCol extends BaseNavCol
{
    private final SuperAdminLayout superAdminLayout;

    public NavCol(SuperAdminLayout superAdminLayout)
    {
        setLayout(this, MARGIN_TL);
        this.superAdminLayout = superAdminLayout;
    }

    protected void addLinks()
    {
        addComponent(getLink(SuperAdminPageType.ORGS));
        addComponent(getLink(SuperAdminPageType.USERS));

        addSpacer(SuperAdminPageType.ORGS.toString().length() + 20);
    }

    protected void setPage(AdminPageType pageType)
    {
        superAdminLayout.setAdminPage(pageType);
    }
}
