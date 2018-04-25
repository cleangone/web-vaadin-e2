package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event;

import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.AdminPageType;

public enum EventAdminPageType implements AdminPageType
{
    EVENTS("All Events"),
    GENERAL("General"),
    PARTICIPANTS("Participants"),
    TAGS("Tags"),
    CATEGORIES("Categories"),
    ITEMS("Items"),
    DATES("Dates"),
    ROLES("User Roles"),
    USERS("Users"),
    DONATIONS("Donations"),
    PURCHASES("Purchases");

    private final String text;
    EventAdminPageType(final String text)
    {
        this.text = text;
    }
    public String toString()
    {
        return text;
    }
}

