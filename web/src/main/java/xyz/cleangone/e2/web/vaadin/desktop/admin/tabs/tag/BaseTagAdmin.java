package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.tag;

import com.vaadin.ui.Grid;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

public abstract class BaseTagAdmin extends BaseAdmin
{
    protected final TagsAdminLayout tagsAdminLayout;

    public BaseTagAdmin(TagsAdminLayout tagsAdminLayout, MessageDisplayer msgDisplayer)
    {
        super(msgDisplayer);
        this.tagsAdminLayout = tagsAdminLayout;
    }
}