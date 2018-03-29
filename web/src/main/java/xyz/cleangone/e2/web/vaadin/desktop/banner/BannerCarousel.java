package xyz.cleangone.e2.web.vaadin.desktop.banner;

import static xyz.cleangone.e2.web.vaadin.desktop.banner.BannerUtil.*;
import com.vaadin.ui.AbsoluteLayout;
import org.vaadin.virkki.carousel.HorizontalCarousel;
import org.vaadin.virkki.carousel.client.widget.gwt.ArrowKeysMode;
import org.vaadin.virkki.carousel.client.widget.gwt.CarouselLoadMode;
import xyz.cleangone.data.aws.dynamo.entity.base.OrgLastTouched;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.e2.web.manager.SessionManager;

import java.util.HashMap;
import java.util.Map;


public class BannerCarousel extends HorizontalCarousel implements BannerComponent
{
    private OrgLastTouched orgLastTouched = null;
    private AbsoluteLayout orgBanner;

    public BannerCarousel()
    {
        setWidth(getBannerWidth());
        setHeight(getBannerHeight());
        setArrowKeysMode(ArrowKeysMode.FOCUS);
        setLoadMode(CarouselLoadMode.SMART);
        setTransitionDuration(500);
    }

    public void reset(SessionManager sessionMgr)
    {
        OrgManager orgMgr = sessionMgr.getOrgManager();

        if (!bannerIsCurrent(orgMgr))
        {
            removeAllComponents();
            orgLastTouched = orgMgr.getOrgLastTouched();

            EventManager eventMgr = sessionMgr.getEventManager();
            for (OrgEvent event : eventMgr.getActiveEvents())
            {
                if (!event.getUseOrgBanner())
                {
                    AbsoluteLayout eventBanner = getBanner(event);
                    addComponent(eventBanner);
                    addComponentToLayout(getHtml(event, sessionMgr, getUI()), eventBanner);
                }
            }

            // add org last so we can see scrolling
            Organization org = orgMgr.getOrg();
            orgBanner = getBanner(org);
            addComponent(orgBanner);
            addComponentToLayout(getHtml(org, getUI()), orgBanner);
        }

        scrollTo(orgBanner);
    }

    private boolean bannerIsCurrent(OrgManager orgMgr)
    {
        if (orgLastTouched == null) { return false; }

        Organization org = orgMgr.getOrg();
        if (!orgLastTouched.getId().equals(org.getId())) { return false; }

        OrgLastTouched currlastTouch = orgMgr.getOrgLastTouched();
        return orgLastTouched.equals(currlastTouch);
    }
}
