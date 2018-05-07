package xyz.cleangone.e2.web.vaadin.desktop;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;

import com.vaadin.annotations.*;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.dao.CatalogItemDao;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.aws.dynamo.entity.person.UserToken;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.manager.VaadinSessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.EventAdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.admin.OrgAdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.admin.superadmin.SuperAdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.ActionBar;
import xyz.cleangone.e2.web.vaadin.desktop.admin.superadmin.SuperAdminProfilePage;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.stats.browser.BrowserStats;
import xyz.cleangone.e2.web.vaadin.desktop.broadcast.BroadcastListener;
import xyz.cleangone.e2.web.vaadin.desktop.broadcast.BroadcastListeningUI;
import xyz.cleangone.e2.web.vaadin.desktop.org.*;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.CatalogPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.ItemPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.payment.IatsPaymentPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.payment.PaymentPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.profile.BidsPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.profile.ProfilePage;
import xyz.cleangone.e2.web.vaadin.desktop.org.profile.ProfilePageType;
import xyz.cleangone.e2.web.vaadin.desktop.org.profile.WatchLayout;
import xyz.cleangone.e2.web.vaadin.desktop.user.LoginPage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Push
@Theme("mytheme")
@Viewport("user-scalable=yes,initial-scale=1.0")
public class MyUI extends BroadcastListeningUI implements BroadcastListener
{
    public static final String RESET_PASSWORD_URL_PARAM = "reset";
    public static final String VERIFY_EMAIL_URL_PARAM = "verify";
    public static final String ITEM_URL_PARAM = "item";
    public static final BrowserStats BROWSER_STATS = new BrowserStats();
    public static boolean COLORS = false;

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet { }

    private static final Logger LOG = Logger.getLogger(MyUI.class.getName());

    @Override
    protected void init(VaadinRequest vaadinRequest)
    {
        BROWSER_STATS.addPage(getCurrent().getPage());

        new Navigator(this, this);
        initBroadcastListener();
        UI.getCurrent().setResizeLazy(true);
        ActionBar.addActionBarStyle();

        SessionManager sessionMgr = VaadinSessionManager.createSessionManager();
        sessionMgr.resetEventViews();

        WebBrowser webBrowser = getCurrent().getPage().getWebBrowser();
        sessionMgr.setIsMobileBrowser(webBrowser.isIOS() || webBrowser.isAndroid() || webBrowser.isWindowsPhone());

        // strip off # qualifier and/or ? params
        String uri = vaadinRequest.getParameter("v-loc");
        if (uri.contains("?")) { uri = uri.substring(0, uri.indexOf("?")); }
        if (uri.contains("#")) { uri = uri.substring(0, uri.indexOf("#")); }
        sessionMgr.setUrl(uri);

        View loginPage = new LoginPage();
        OrgPage orgPage = new OrgPage(sessionMgr.isMobileBrowser());

        Navigator nav = getNavigator();
        nav.addView(LoginPage.NAME, loginPage);
        nav.addView(OrgPage.NAME, orgPage);
        nav.addView(SuperAdminPage.NAME, new SuperAdminPage());
        nav.addView(SuperAdminProfilePage.NAME, new SuperAdminProfilePage());
        nav.addView(OrgAdminPage.NAME, new OrgAdminPage());
        nav.addView(EventAdminPage.NAME, new EventAdminPage());
        nav.addView(CalendarPage.NAME, new CalendarPage());
        nav.addView(CatalogPage.NAME, new CatalogPage());
        nav.addView(SigninPage.NAME, new SigninPage());
        nav.addView(PasswordRequestPage.NAME, new PasswordRequestPage());
        nav.addView(PasswordResetPage.NAME, new PasswordResetPage());
        nav.addView(ProfilePage.NAME, new ProfilePage());
        nav.addView(BidsPage.NAME, new BidsPage(ProfilePageType.BIDS));
        nav.addView(BidsPage.WATCH_NAME, new BidsPage(ProfilePageType.WATCHED));
        nav.addView(CreateAccountPage.NAME, new CreateAccountPage());
        nav.addView(CartPage.NAME, new CartPage());
        nav.addView(PaymentPage.NAME, new PaymentPage());
        nav.addView(IatsPaymentPage.NAME, new IatsPaymentPage());

        UserManager userMgr = sessionMgr.getUserManager();
        String resetPasswordToken = vaadinRequest.getParameter(RESET_PASSWORD_URL_PARAM);
        if (resetPasswordToken != null)
        {
            User user = userMgr.loginByToken(resetPasswordToken);
            if (user != null)
            {
                // get org from path - pathTags should always be set - request password always from an org
                // todo - user could nav to another org before requesting reset
                List<String> pathTags = getPathTags(vaadinRequest);
                if (!pathTags.isEmpty())
                {
                    getOrgFromPath(pathTags, sessionMgr.getOrgManager()); // does setOrg
                    sessionMgr.resetEventManager();
                }

                nav.setErrorView(orgPage);
                nav.navigateTo(PasswordResetPage.NAME);
                return;
            }
        }

        loginByCookie(userMgr);
        verifyEmail(vaadinRequest, userMgr);

        String initialPage = getInitialPage(vaadinRequest, sessionMgr);  // parse url /<orgTag>/<eventTag>
        if (initialPage == null && userMgr.userIsSuperAdmin())
        {
            initialPage = SuperAdminPage.NAME;
        }

        // check for direct link to item
        String itemPage = getItemPage(vaadinRequest, sessionMgr);
        if (itemPage != null) { initialPage = itemPage; }

        nav.setErrorView(initialPage == null ? loginPage : orgPage);
        nav.navigateTo(initialPage == null ? LoginPage.NAME : initialPage);
    }

    private boolean loginByCookie(UserManager userMgr)
    {
        // check for token
        Cookie userCookie = VaadinSessionManager.getUserCookie();
        if (userCookie != null && userCookie.getValue() != null && userCookie.getValue().length() > 0)
        {
            User user = userMgr.loginByToken(userCookie.getValue());
            if (user == null)
            {
                // cookie is old
                VaadinSessionManager.clearUserCookie();
            }
            else
            {
                UserToken newToken = userMgr.cycleToken();
                VaadinSessionManager.setUserCookie(newToken.getId());
                return true;
            }
        }

        return false;
    }

    private void verifyEmail(VaadinRequest vaadinRequest, UserManager userMgr)
    {
        String verifyEmailToken = vaadinRequest.getParameter(VERIFY_EMAIL_URL_PARAM);

        if (verifyEmailToken == null) { return; }

        userMgr.verifyEmail(verifyEmailToken);
    }

    // url path may contain /<orgTag>/<eventTag>
    private String getInitialPage(VaadinRequest vaadinRequest, SessionManager sessionMgr)
    {
        List<String> pathTags = getPathTags(vaadinRequest);
        if (pathTags.isEmpty()) { return null; }

        // first tag is org
        String returnPage = null;
        Organization org = getOrgFromPath(pathTags, sessionMgr.getOrgManager());
        if (org != null) { returnPage = OrgPage.NAME; }

        // second tag is an event if it exists
        if (pathTags.size() == 1) { return returnPage; }
        EventManager eventMgr = sessionMgr.getEventManager();
        OrgEvent event = eventMgr.getActiveEvent(pathTags.get(1));
        if (event != null)
        {
            eventMgr.setEvent(event);
            returnPage = EventPage.NAME;
        }

        return returnPage;
    }


    // url path may contain /<orgTag>/<eventTag>
    private List<String> getPathTags(VaadinRequest vaadinRequest)
    {
        String path = vaadinRequest.getPathInfo();
        if (path == null || !path.startsWith("/") || path.equals("/"))
        {
            return null;
        }

        return Arrays.asList(path.substring(1).split("\\s*/\\s*"));  // can be empty
    }

    private Organization getOrgFromPath(List<String> pathTags, OrgManager orgMgr)
    {
        // first tag is org
        Organization org = orgMgr.findOrg(pathTags.get(0));
        if (org != null)
        {
//            verifyUser(userMgr, org);
            orgMgr.setOrg(org);
        }

        return org;
    }



        // todo - quite a mess
    private String getItemPage(VaadinRequest vaadinRequest, SessionManager sessionMgr)
    {
        String itemId = vaadinRequest.getParameter(ITEM_URL_PARAM);
        if (itemId == null) { return null; }

        // todo - feels wrong to go directly after dao
        CatalogItemDao itemDao = new CatalogItemDao();
        CatalogItem item = itemDao.getById(itemId);

        OrgManager orgMgr = sessionMgr.getOrgManager();
        if (orgMgr.getOrgId() == null) { orgMgr.setOrgById(item.getOrgId()); }
        else if (!orgMgr.getOrgId().equals(item.getOrgId())) { return null; } // org mismatch - should not have happened

        // set event based on item
        EventManager eventMgr = sessionMgr.getResetEventManager();  // event, category set to null
        for (OrgEvent event : eventMgr.getActiveEvents())
        {
            if (event.getId().equals(item.getEventId())) { eventMgr.setEvent(event); }
        }

        if (eventMgr.getEvent() == null) { return null; } // item's event no longer active

        // get category - overlaps with BidAdmin
        OrgTag category = getCategory(item, orgMgr.getTagManager());
        if (category == null) { return null; } // cannot find category

        eventMgr.setCategory(category);
        eventMgr.setItem(item);
        getNavigator().addView(ItemPage.NAME, new ItemPage());  // todo - why do this?  Same qustion as BidsAdmin
        return ItemPage.NAME;
    }

    private OrgTag getCategory(CatalogItem item, TagManager tagMgr)
    {
        Map<String, OrgTag> categoryIdToCategory = new HashMap<>();
        for (OrgTag category : tagMgr.getCategories()) { categoryIdToCategory.put(category.getId(), category); }

        for (String itemCategoryId : item.getCategoryIds())
        {
            if (categoryIdToCategory.containsKey(itemCategoryId))
            {
                return categoryIdToCategory.get(itemCategoryId);
            }
        }

        return null;
    }

    private void verifyUser(UserManager userMgr, Organization org)
    {
//        User user = userMgr.getUser();
//        if (user != null && user.getOrgId() != null && !user.getOrgId().equals(org.getId()))
//        {
//            LOG.info("Logging out User " + user.getId() + " because logged in by token but org " + user.getOrgId() +
//                " does not match path Org " + org.getId());
//            userMgr.logout();
//        }
    }
}
