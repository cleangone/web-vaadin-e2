package xyz.cleangone.e2.web.vaadin.desktop.admin.superadmin;

import xyz.cleangone.e2.web.vaadin.desktop.org.profile.ProfileAdmin;

public class SuperAdminProfilePage extends BaseSuperAdminPage
{
    public static final String NAME = "SuperAdminProfile";
    public static final String DISPLAY_NAME = "User Profile";

    protected void set()
    {
        mainLayout.removeAllComponents();

        ProfileAdmin profileAdmin = new ProfileAdmin(actionBar);
        profileAdmin.set(sessionMgr);
        mainLayout.addComponent(profileAdmin);
    }
}
