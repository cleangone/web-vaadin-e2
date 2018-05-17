package xyz.cleangone.e2.web.vaadin.desktop.admin.superadmin;

public class SuperAdminPage extends BaseSuperAdminPage
{
    public static final String NAME = "Super";
    public static final String DISPLAY_NAME = "Super Admin";

    protected void set()
    {
        mainLayout.removeAllComponents();

        SuperAdminLayout superAdminLayout = new SuperAdminLayout(actionBar);
        superAdminLayout.set(sessionMgr);
        mainLayout.addComponent(superAdminLayout);
    }
}
