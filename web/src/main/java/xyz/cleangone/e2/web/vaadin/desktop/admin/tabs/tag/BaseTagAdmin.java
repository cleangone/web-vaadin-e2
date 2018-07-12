package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.tag;

import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.BaseAdmin;
import xyz.cleangone.web.vaadin.ui.MessageDisplayer;

public abstract class BaseTagAdmin extends BaseAdmin
{
    protected final TagsAdminLayout tagsAdminLayout;

    public BaseTagAdmin(TagsAdminLayout tagsAdminLayout, MessageDisplayer msgDisplayer)
    {
        super(msgDisplayer);
        this.tagsAdminLayout = tagsAdminLayout;
    }
}