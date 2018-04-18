package xyz.cleangone.e2.web.vaadin.desktop.actionbar;

import xyz.cleangone.data.aws.dynamo.entity.base.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.EventAdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.admin.OrgAdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.admin.superadmin.SuperAdminPageNew;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;

import java.util.List;

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

        if (sessionMgr.hasSuperUser())
        {
            addNavigateItem(SuperAdminPageNew.NAME, SuperAdminPageNew.DISPLAY_NAME, this);
        }

        PageDisplayType pageDisplayType = PageDisplayType.NoChange;
        if (sessionMgr.hasOrg() && sessionMgr.hasUser())
        {
            TagManager tagMgr = sessionMgr.getOrgManager().getTagManager();
            if (userMgr.userIsAdmin(org)) { addNavigateItem(OrgAdminPage.NAME, OrgAdminPage.DISPLAY_NAME, this); }
            else
            {
                List<OrgTag> tags = tagMgr.getEventAdminRoleTags();
                pageDisplayType = PageDisplayType.ObjectRetrieval;
                if (userMgr.userHasEventAdmin(org, tags)) { addNavigateItem(EventAdminPage.NAME, EventAdminPage.DISPLAY_NAME, this); }
            }
        }

        return pageDisplayType;
    }
}
