package xyz.cleangone.e2.web.vaadin.desktop.actionbar;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.MenuBar;
import xyz.cleangone.data.aws.dynamo.entity.lastTouched.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.EventAdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.admin.OrgAdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.admin.superadmin.SuperAdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.OrgPage;
import xyz.cleangone.web.vaadin.ui.PageDisplayType;

public class LeftMenuBar extends BaseMenuBar
{
    public PageDisplayType set(SessionManager sessionMgr)
    {
        super.set(sessionMgr);
        return set();
    }

    private PageDisplayType set()
    {
        User user = userMgr.getUser();
        Organization org = sessionMgr.getOrg();

        if (changeManager.unchanged(user) &&
            changeManager.unchanged(org) &&
            changeManager.unchanged(user, EntityType.Entity) &&
            changeManager.unchanged(org, EntityType.Entity, EntityType.Tag))
        {
            return PageDisplayType.NoChange;
        }

        changeManager.reset(user);
        removeItems();

        return sessionMgr.isMobileBrowser() ?  addMobileItems() : addItems(org);
    }

    private PageDisplayType addItems(Organization org)
    {
        PageDisplayType pageDisplayType = PageDisplayType.NoChange;
        if (sessionMgr.hasOrg() && sessionMgr.hasUser())
        {
            TagManager tagMgr = sessionMgr.getOrgManager().getTagManager();
            if (userMgr.userIsOrgAdmin(org.getId()))
            {
                addIconOnlyItem(OrgAdminPage.DISPLAY_NAME, VaadinIcons.WRENCH, getNavigateCmd(OrgAdminPage.NAME));
            }
            else
            {
//                List<OrgTag> tags = tagMgr.getEventAdminRoleTags();
                pageDisplayType = PageDisplayType.ObjectRetrieval;
                if (userMgr.userIsEventAdmin(org.getId()))
                {
                    addIconOnlyItem(EventAdminPage.DISPLAY_NAME, VaadinIcons.WRENCH, getNavigateCmd(EventAdminPage.NAME));
                }
            }
        }

        if (sessionMgr.hasSuperUser())
        {
            addIconOnlyItem(SuperAdminPage.DISPLAY_NAME, VaadinIcons.SITEMAP, getNavigateCmd(SuperAdminPage.NAME));
        }

        return pageDisplayType;
    }

    private PageDisplayType addMobileItems()
    {
        Organization org = sessionMgr.getOrg();
        if (org!= null)
        {
            MenuBar.MenuItem menuItem = addItem("", VaadinIcons.MENU, null);
            menuItem.addItem("Home", null, getNavigateCmd(OrgPage.NAME));
            addEvents(menuItem);
        }

        return PageDisplayType.NoChange;
    }
}
