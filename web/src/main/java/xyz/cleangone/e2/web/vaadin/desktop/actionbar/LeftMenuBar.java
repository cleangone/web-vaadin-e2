package xyz.cleangone.e2.web.vaadin.desktop.actionbar;

import xyz.cleangone.data.aws.dynamo.entity.base.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.AdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.admin.SuperAdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;

import java.util.Date;
import java.util.List;
import static xyz.cleangone.e2.web.vaadin.util.PageUtils.*;


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

        if (sessionMgr.hasSuperUser()) { addNavigateItem(SuperAdminPage.NAME, this); }

        PageDisplayType pageDisplayType = PageDisplayType.NoChange;
        if (sessionMgr.hasOrg() && sessionMgr.hasUser())
        {
            TagManager tagMgr = sessionMgr.getOrgManager().getTagManager();
            if (userMgr.userIsAdmin(org)) { addNavigateItem(AdminPage.NAME, this); }
            else
            {
                List<OrgTag> tags = tagMgr.getEventAdminRoleTags();
                pageDisplayType = PageDisplayType.ObjectRetrieval;
                if (userMgr.userHasEventAdmin(org, tags)) { addNavigateItem(AdminPage.NAME, this); }
            }
        }

        return pageDisplayType;
    }
}
