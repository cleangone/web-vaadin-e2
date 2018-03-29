package xyz.cleangone.e2.web.vaadin.desktop.banner;

import static xyz.cleangone.e2.web.vaadin.desktop.banner.BannerUtil.*;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.HorizontalLayout;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.e2.web.manager.SessionManager;

public class BannerSingle extends HorizontalLayout implements BannerComponent
{
    public BannerSingle()
    {
        setWidth(getBannerWidth());
        setHeight(getBannerHeight());
    }

    public void reset(SessionManager sessionMgr)
    {
        removeAllComponents();

        EventManager eventMgr = sessionMgr.getEventManager();
        OrgEvent event = eventMgr == null ? null : eventMgr.getEvent();
        if (event != null && !event.getUseOrgBanner())
        {
            AbsoluteLayout eventBanner = getBanner(event);
            addComponent(eventBanner);
            addComponentToLayout(getHtml(event, sessionMgr, getUI()), eventBanner);
        }
        else
        {
            OrgManager orgMgr = sessionMgr.getOrgManager();
            Organization org = orgMgr.getOrg();

            AbsoluteLayout orgBanner = getBanner(org);
            addComponent(orgBanner);
            addComponentToLayout(getHtml(org, getUI()), orgBanner);
        }
    }

}