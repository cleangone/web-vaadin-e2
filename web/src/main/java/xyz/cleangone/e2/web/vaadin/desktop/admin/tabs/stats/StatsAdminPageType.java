package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.stats;

import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.AdminPageType;

public enum StatsAdminPageType implements AdminPageType
{
    CACHE("Cache Stats"),
    PAGE("Page Stats");

    private final String text;
    StatsAdminPageType(final String text)
    {
        this.text = text;
    }
    public String toString()
    {
        return text;
    }
}

