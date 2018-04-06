package xyz.cleangone.e2.web.vaadin.desktop.org;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.manager.VaadinSessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.ActionBar;
import xyz.cleangone.e2.web.vaadin.desktop.banner.BannerComponent;
import xyz.cleangone.e2.web.vaadin.desktop.banner.BannerCarousel;
import xyz.cleangone.e2.web.vaadin.desktop.banner.BannerSingle;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static xyz.cleangone.e2.web.manager.PageStats.*;


public abstract class BasePage extends Panel implements View
{
    protected enum BannerStyle { Carousel, Single };

    private VerticalLayout pageLayout = new VerticalLayout();
    private BannerComponent banner;
    protected ActionBar actionBar = new ActionBar();
    protected AbstractOrderedLayout mainLayout;

    protected SessionManager sessionMgr;
    protected OrgManager orgMgr;

    // todo - deprecated - replace w/ entityLastTouched
    private Map<String, Date> entityIdToUpdateDate = new HashMap<>();

    public BasePage(BannerStyle bannerStyle)
    {
        this(new VerticalLayout(), bannerStyle);
        mainLayout.setMargin(false);
        mainLayout.setSpacing(false);
    }

    public BasePage(AbstractOrderedLayout mainLayout, BannerStyle bannerStyle)
    {
        this.mainLayout = mainLayout;

        // components fills the browser screen
        setSizeFull();

        // pageLayout sits in components, scrolls if doesn't fit
        pageLayout.setMargin(false);
        pageLayout.setSpacing(false);

        banner = (bannerStyle == BannerStyle.Carousel) ? new BannerCarousel() : new BannerSingle();
        pageLayout.addComponents(banner, actionBar, mainLayout);
        pageLayout.setExpandRatio(mainLayout, 1.0f);

        setContent(pageLayout);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event)
    {
        Date start = new Date();
        SessionManager sessionManager = VaadinSessionManager.getExpectedSessionManager();
        if (sessionManager.hasOrg())
        {
            PageDisplayType pageDisplayType = set(sessionManager);
            addRetrievalTime(sessionManager.getOrg().getId(), getPageName(), pageDisplayType, start);
        }
    }

    protected String getPageName()
    {
        return null;
    }

    protected PageDisplayType set(SessionManager sessionMgr)
    {
        this.sessionMgr = sessionMgr;
        orgMgr = sessionMgr.getOrgManager();

        return PageDisplayType.NotApplicable;
    }

    protected PageDisplayType resetHeader()
    {
        banner.reset(sessionMgr);
        return actionBar.set(sessionMgr);
    }

    protected boolean updateDatesChanged(BaseEntity entity, List<? extends BaseEntity> entities)
    {
        return (entityIdToUpdateDate.size() != entities.size() + 1 ||
            updateDateChanged(entity) ||
            updateDatesChanged(entities));
    }

    protected boolean updateDateChanged(BaseEntity entity)
    {
        return !entityIdToUpdateDate.containsKey(entity.getId()) ||
            !entityIdToUpdateDate.get(entity.getId()).equals(entity.getUpdatedDate());
    }

    protected boolean updateDatesChanged(List<? extends BaseEntity> entities)
    {
        for (BaseEntity entity : entities)
        {
            if (updateDateChanged(entity)) { return true; }
        }

        return false;
    }

    protected void setUpdateDate(BaseEntity entity)
    {
        entityIdToUpdateDate.clear();
        addUpdateDate(entity);
    }

    protected void setUpdateDates(BaseEntity entity, List<? extends BaseEntity> entities)
    {
        setUpdateDate(entity);
        entities.forEach(this::addUpdateDate);
    }



    private void addUpdateDate(BaseEntity entity) { entityIdToUpdateDate.put(entity.getId(), entity.getUpdatedDate()); }

    protected void navigateTo(OrgEvent event)
    {
        sessionMgr.navigateTo(event, getUI().getNavigator());
    }
    protected void navigateTo(String pageName)
    {
        getUI().getNavigator().navigateTo(pageName);
    }

    protected void showError(String msg) { Notification.show(msg, Notification.Type.ERROR_MESSAGE); }

}
