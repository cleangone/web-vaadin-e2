package xyz.cleangone.e2.web.vaadin.desktop.actionbar;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.purchase.Cart;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.EntityChangeManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static xyz.cleangone.e2.web.vaadin.desktop.actionbar.ActionBar.ACTION_BAR_STYLE_NAME;

public class BaseMenuBar extends MenuBar
{
    protected SessionManager sessionMgr;
    protected UserManager userMgr;
    protected EventManager eventMgr;
    protected EntityChangeManager changeManager = new EntityChangeManager();

    public BaseMenuBar()
    {
        addStyleName(ValoTheme.MENUBAR_BORDERLESS);
    }

    protected PageDisplayType set(SessionManager sessionMgr)
    {
        this.sessionMgr = requireNonNull(sessionMgr);
        userMgr = sessionMgr.getUserManager();
        eventMgr = sessionMgr.getEventManager();
        return PageDisplayType.NotApplicable;
    }

    protected void addEvents(MenuBar.MenuItem parentMenuItem)
    {
        List<OrgEvent> events = eventMgr.getActiveEvents();
        if (!events.isEmpty())
        {
            for (OrgEvent event : events)
            {
                if (!event.getUseOrgBanner())
                {
                    parentMenuItem.addItem(event.getName(), null, new MenuBar.Command() {
                        public void menuSelected(MenuBar.MenuItem selectedItem) {
                            sessionMgr.navigateTo(event, getUI().getNavigator());
                        }
                    });
                }
            }
        }
    }

    protected void addNavigateItem(String pageName, Resource icon, MenuBar menuBar)
    {
        MenuItem menuItem = menuBar.addItem("", null, getNavigateCmd(pageName));
        setMenuItem(menuItem, icon, pageName);
    }

    protected void setMenuItem(MenuItem menuItem, Resource icon, String description)
    {
        menuItem.setIcon(icon);
        menuItem.setStyleName("icon-only");
        menuItem.setDescription(description);
    }

    protected void addNavigateItem(String pageName, MenuBar menuBar)
    {
        addNavigateItem(pageName, pageName, menuBar);
    }

    protected void addNavigateItem(String pageName, String caption, MenuBar menuBar)
    {
        menuBar.addItem(caption, null, getNavigateCmd(pageName));
    }

    protected Command getNavigateCmd(String pageName)
    {
        return new Command() {
            public void menuSelected(MenuItem selectedItem) { navigateTo(pageName); }
        };
    }

    protected void addIconOnlyItem(String description, Resource icon, MenuBar.Command command)
    {
        MenuBar.MenuItem menuItem = addItem("",  icon, command);
        menuItem.setStyleName("icon-only");
        menuItem.setDescription(description);
    }

    protected void navigateTo(String pageName) { getUI().getNavigator().navigateTo(pageName); }

}
