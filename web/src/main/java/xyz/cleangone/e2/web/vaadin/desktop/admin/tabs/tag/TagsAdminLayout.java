package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.tag;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.AdminPageType;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.*;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import java.util.HashMap;
import java.util.Map;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class TagsAdminLayout extends HorizontalLayout
{
    private final TagsNavCol navCol;
    private final Map<AdminPageType, BaseAdmin> adminComponents = new HashMap<>();
    private final VerticalLayout mainLayout = new VerticalLayout();

    private AdminPageType adminPageType;

    public TagsAdminLayout(MessageDisplayer msgDisplayer)
    {
        setLayout(this, MARGIN_FALSE, SIZE_FULL, BACK_BLUE);

        navCol = new TagsNavCol(this);
        adminComponents.put(TagAdminPageType.TAG_TYPES, new TagTypesAdmin(this, msgDisplayer));
        adminComponents.put(TagAdminPageType.TAG_TYPE, new TagAdmin(this, msgDisplayer));

        setLayout(mainLayout, MARGIN_FALSE, SIZE_FULL, BACK_RED);
        mainLayout.setHeight((UI.getCurrent().getPage().getBrowserWindowHeight() - 100) + "px");  // hack - like navCol

        addComponents(navCol, mainLayout);
        setExpandRatio(mainLayout, 1.0f);
    }

    public void set(SessionManager sessionMgr)
    {
        navCol.set(sessionMgr);

        UI ui = getUI();
        for (BaseAdmin component : adminComponents.values())
        {
            component.set(sessionMgr, ui);
        }

        // start with display of all events
        setAdminPage(TagAdminPageType.TAG_TYPES);
    }

    public void setNav()
    {
        navCol.set();
    }

    public AdminPageType getAdminPageType()
    {
        return adminPageType;
    }
    public void setAdminPage(AdminPageType pageType)
    {
        adminPageType = pageType;
        navCol.setLinks(pageType);

        BaseAdmin component = adminComponents.get(pageType);
        component.set();

        setSpacing(pageType != EventAdminPageType.ITEMS && pageType != EventAdminPageType.PARTICIPANTS);

        mainLayout.removeAllComponents();
        mainLayout.addComponent(component);
    }
}