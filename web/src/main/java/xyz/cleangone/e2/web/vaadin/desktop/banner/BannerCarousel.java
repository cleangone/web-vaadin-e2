package xyz.cleangone.e2.web.vaadin.desktop.banner;

import static xyz.cleangone.e2.web.vaadin.desktop.banner.BannerUtil.*;
import com.vaadin.ui.AbsoluteLayout;
import org.vaadin.virkki.carousel.HorizontalCarousel;
import org.vaadin.virkki.carousel.client.widget.gwt.ArrowKeysMode;
import org.vaadin.virkki.carousel.client.widget.gwt.CarouselLoadMode;
import xyz.cleangone.data.aws.dynamo.entity.lastTouched.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.e2.web.manager.EntityChangeManager;
import xyz.cleangone.e2.web.manager.SessionManager;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BannerCarousel extends HorizontalCarousel implements BannerComponent
{
    private AbsoluteLayout orgBanner;
    private EntityChangeManager changeManager = new EntityChangeManager();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public BannerCarousel()
    {
        setWidth(getBannerWidth());
        setHeight(getBannerHeight());
        //setArrowKeysMode(ArrowKeysMode.FOCUS);
        setArrowKeysMode(ArrowKeysMode.ALWAYS);


        setLoadMode(CarouselLoadMode.SMART);
        setTransitionDuration(500);
    }

    public void reset(SessionManager sessionMgr)
    {
        resetBanner(sessionMgr);
        scrollTo(orgBanner);
        //scheduler.scheduleAtFixedRate(new BannerScroller(this), 2, 2, SECONDS);
    }

    private void resetBanner(SessionManager sessionMgr)
    {
        OrgManager orgMgr = sessionMgr.getOrgManager();
        Organization org = orgMgr.getOrg();

        if (changeManager.unchanged(org) &&
            changeManager.unchanged(org, EntityType.Entity, EntityType.Event))
        {
            return;
        }

        changeManager.reset(org);
        removeAllComponents();

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
        orgBanner = getBanner(org);
        addComponent(orgBanner);
        addComponentToLayout(getHtml(org, sessionMgr.isMobileBrowser(), getUI()), orgBanner);
    }

    // todo - doesn't work - only scrolls once an arrow clicked
    int i=0;
    private void scroll()
    {
        System.out.println("scrolling " + i++);
        scroll(1); // specifies direction and steps
    }

    class BannerScroller implements Runnable
    {
        private final BannerCarousel bannerCarousel;

        public BannerScroller(BannerCarousel bannerCarousel)
        {
            this.bannerCarousel = bannerCarousel;
        }

        public void run()
        {
            bannerCarousel.scroll();
        }
    }
}
