package xyz.cleangone.e2.web.vaadin.desktop;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;

import com.vaadin.annotations.*;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.aws.dynamo.entity.person.UserToken;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.manager.VaadinSessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.AdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.admin.SuperAdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.banner.ActionBar;
import xyz.cleangone.e2.web.vaadin.desktop.org.*;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.CatalogPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.payment.BraintreePaymentPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.payment.IatsPaymentPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.payment.PaymentPage;
import xyz.cleangone.e2.web.vaadin.desktop.user.LoginPage;
import xyz.cleangone.util.Crypto;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Theme("mytheme")
public class MyUI extends UI
{
    public static final String RESET_PASSWORD_URL_PARAM = "reset";
    public static final String VERIFY_EMAIL_URL_PARAM = "verify";

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet { }

    private static final Logger LOG = Logger.getLogger(MyUI.class.getName());

    @Override
    protected void init(VaadinRequest vaadinRequest)
    {
        new Navigator(this, this);
        ActionBar.addActionBarStyle();

        SessionManager sessionMgr = VaadinSessionManager.createSessionManager();
        sessionMgr.resetEventViews();

        // strip off # qualifier and/or ? params
        String uri = vaadinRequest.getParameter("v-loc");
        if (uri.contains("?")) { uri = uri.substring(0, uri.indexOf("?")); }
        if (uri.contains("#")) { uri = uri.substring(0, uri.indexOf("#")); }
        sessionMgr.setUrl(uri);

        View loginPage = new LoginPage();
        OrgPage orgPage = new OrgPage();

        Navigator nav = getNavigator();
        nav.addView(LoginPage.NAME, loginPage);
        nav.addView(OrgPage.NAME, orgPage);
        nav.addView(SuperAdminPage.NAME, new SuperAdminPage());
        nav.addView(AdminPage.NAME, new AdminPage());
        nav.addView(CalendarPage.NAME, new CalendarPage());
        nav.addView(CatalogPage.NAME, new CatalogPage());
        nav.addView(SigninPage.NAME, new SigninPage());
        nav.addView(PasswordRequestPage.NAME, new PasswordRequestPage());
        nav.addView(PasswordResetPage.NAME, new PasswordResetPage());
        nav.addView(ProfilePage.NAME, new ProfilePage());
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
                sessionMgr.getOrgManager().setOrgById(user.getOrgId());
                sessionMgr.resetEventManager();

                getNavigator().setErrorView(orgPage);
                getNavigator().navigateTo(PasswordResetPage.NAME);
                return;
            }
        }

        boolean loggedIn = loginByCookie(userMgr);  // return var for debugging
        verifyEmail(vaadinRequest, userMgr);

        String initialPage = getInitialPage(vaadinRequest, sessionMgr);
        if (initialPage == null && userMgr.userIsSuper())
        {
            initialPage = SuperAdminPage.NAME;
        }

        getNavigator().setErrorView(initialPage == null ? loginPage : orgPage);
        getNavigator().navigateTo(initialPage == null ? LoginPage.NAME : initialPage);
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

    private String getInitialPage(VaadinRequest vaadinRequest, SessionManager sessionMgr)
    {
        String path = vaadinRequest.getPathInfo();
        if (path == null || !path.startsWith("/") || path.equals("/")) { return null; }

        List<String> tags = Arrays.asList(path.substring(1).split("\\s*/\\s*"));
        if (tags.isEmpty()) { return null; }

        String returnPage = null;
        OrgManager orgMgr = sessionMgr.getOrgManager();
        UserManager userMgr = sessionMgr.getUserManager();

         // first tag is org
        Organization org = orgMgr.findOrg(tags.get(0));
        if (org != null)
        {
            verifyUser(userMgr, org);
            orgMgr.setOrg(org);
            returnPage = OrgPage.NAME;
        }

        if (tags.size() == 1) { return returnPage; }

        // second tag is an event if it exists
        EventManager eventMgr = sessionMgr.getEventManager();
        OrgEvent event = eventMgr.getActiveEvent(tags.get(1));
        if (event != null)
        {
            eventMgr.setEvent(event);
            returnPage = EventPage.NAME;
        }

        return returnPage;
    }

    private void verifyUser(UserManager userMgr, Organization org)
    {
        User user = userMgr.getUser();
        if (user != null && user.getOrgId() != null && !user.getOrgId().equals(org.getId()))
        {
            LOG.info("Logging out User " + user.getId() + " because logged in by token but org " + user.getOrgId() +
                " does not match path Org " + org.getId());
            userMgr.logout();
        }
    }

}
