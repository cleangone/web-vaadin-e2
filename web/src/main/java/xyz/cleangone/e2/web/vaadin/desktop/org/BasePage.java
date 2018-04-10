package xyz.cleangone.e2.web.vaadin.desktop.org;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.e2.web.manager.EntityChangeManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.manager.VaadinSessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.ActionBar;
import xyz.cleangone.e2.web.vaadin.desktop.banner.BannerComponent;
import xyz.cleangone.e2.web.vaadin.desktop.banner.BannerCarousel;
import xyz.cleangone.e2.web.vaadin.desktop.banner.BannerSingle;

import java.util.Date;
import static xyz.cleangone.e2.web.manager.PageStats.*;


public abstract class BasePage extends Panel implements View
{
    protected static boolean COLORS = false;
    protected enum BannerStyle { Carousel, Single };

    private VerticalLayout pageLayout = new VerticalLayout();
    private BannerComponent banner;
    protected ActionBar actionBar = new ActionBar();
    protected AbstractOrderedLayout mainLayout;

    protected SessionManager sessionMgr;
    protected OrgManager orgMgr;
    protected EntityChangeManager changeManager = new EntityChangeManager();


    public BasePage(BannerStyle bannerStyle)
    {
        this(new VerticalLayout(), bannerStyle);
        mainLayout.setMargin(false);
        mainLayout.setSpacing(false);
    }

    public BasePage(AbstractOrderedLayout mainLayout, BannerStyle bannerStyle)
    {
        this.mainLayout = mainLayout;
        if (COLORS) { mainLayout.addStyleName("backGreen"); }

        // panel fills the browser screen
        setSizeFull();
        if (COLORS) { setStyleName("backOrange"); }

        // pageLayout sits in panel, scrolls if doesn't fit, sadly does not expand because height not 100%
        //
        // From vaadin:
        // if size undefined, layout shrinks to fit the component(s) inside it
        // If you set a VerticalLayout vertically, and there is space left over from the contained components,
        // the extra space is distributed equally between the component cells.
        // if you want one or more components to take all the leftover space. You need to set such a component
        // to 100% size and use setExpandRatio()
        pageLayout.setMargin(false);
        pageLayout.setSpacing(false);
        if (COLORS) { pageLayout.addStyleName("backYellow"); }

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

    // todo - hardcoded w/ banner height and est height of actionbar
    protected int getMainLayoutHeight()
    {
        return UI.getCurrent().getPage().getBrowserWindowHeight() - 300;
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
