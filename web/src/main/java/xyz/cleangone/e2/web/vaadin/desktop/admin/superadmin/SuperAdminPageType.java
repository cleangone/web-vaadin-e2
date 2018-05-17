package xyz.cleangone.e2.web.vaadin.desktop.admin.superadmin;

import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.AdminPageType;

public enum SuperAdminPageType implements AdminPageType
{
    ORGS("Organizations"),
    USERS("Users");

    private final String text;
    SuperAdminPageType(final String text)
    {
        this.text = text;
    }
    public String toString()
    {
        return text;
    }
}

