package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.tag;

import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.AdminPageType;

public enum TagAdminPageType implements AdminPageType
{
    TAG_TYPES("Tag Types"),
    TAG_TYPE("Tag");

    private final String text;
    TagAdminPageType(final String text)
    {
        this.text = text;
    }
    public String toString()
    {
        return text;
    }
}

