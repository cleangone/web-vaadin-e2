package xyz.cleangone.e2.web.vaadin.desktop.user;

import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.aws.dynamo.entity.person.UserToken;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.manager.VaadinSessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.SuperAdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.banner.ActionBar;
import xyz.cleangone.e2.web.vaadin.desktop.banner.BannerCarousel;
import xyz.cleangone.e2.web.vaadin.desktop.banner.BannerSingle;
import xyz.cleangone.e2.web.vaadin.desktop.org.OrgPage;

import javax.servlet.http.Cookie;
import java.util.List;


public class LoginPage extends Panel implements View
{
    public static final String NAME = "Login";

    private MenuBar.MenuItem orgsItem;
    private Panel loginPanel = new Panel("Admin Login");

    public LoginPage()
    {
        // components fills the browser screen
        setSizeFull();

        // pageLayout sits in components, scrolls if doesn't fit
        VerticalLayout pageLayout = new VerticalLayout();
        pageLayout.setMargin(false);
        pageLayout.setSpacing(true);
        pageLayout.setHeight("100%");
        pageLayout.setWidth("100%");
        setContent(pageLayout);

        ActionBar actionBar = new ActionBar();
        actionBar.getLeftMenuBar().removeItems();
        actionBar.getCenterMenuBar().removeItems();
        actionBar.getRightMenuBar().removeItems();
        orgsItem = actionBar.getLeftMenuBar().addItem("Organizations", null, null);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(false);
        mainLayout.setSpacing(false);

        loginPanel.setSizeUndefined();
        mainLayout.addComponent(loginPanel);
        mainLayout.setComponentAlignment(loginPanel, Alignment.MIDDLE_CENTER);

        pageLayout.addComponents(actionBar, mainLayout);
        pageLayout.setExpandRatio(mainLayout, 1.0f);
    }

    @Override
    public void enter(ViewChangeEvent event)
    {
        // session could be new or an existing one from someone who is logging in after browsing
        SessionManager sessionMgr = VaadinSessionManager.createSessionManager();
        UserManager userMgr = sessionMgr.getUserManager();

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

                if (sessionMgr.hasSuperUser())
                {
                    getUI().getNavigator().navigateTo(SuperAdminPage.NAME);
                }
                else
                {
                    // user exists, but is not super
                    OrgManager orgMgr = sessionMgr.getOrgManager();
                    orgMgr.setOrgById(user.getOrgId());

                    getUI().getNavigator().navigateTo(OrgPage.NAME);
                }

                return;
            }
        }

        setOrgs(sessionMgr.getOrgManager());
        setLogin(sessionMgr, userMgr);
    }

    private void setOrgs(OrgManager orgMgr)
    {
        orgsItem.removeChildren();

        List<Organization> orgs = orgMgr.getAll();
        for (Organization org : orgs)
        {
            MenuBar.Command orgCmd = new MenuBar.Command() {
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    orgMgr.setOrg(org);
                    getUI().getNavigator().navigateTo(OrgPage.NAME);
                }
            };

            orgsItem.addItem(org.getName(), null, orgCmd);
        }
    }

    private void setLogin(SessionManager sessionMgr, UserManager userMgr)
    {
        FormLayout layout = new FormLayout();
        loginPanel.setContent(layout);
        layout.setSizeUndefined();
        layout.setMargin(true);
        layout.setSpacing(true);

        TextField emailField = new TextField("Email");
        PasswordField passwordField = new PasswordField("Password");
        CheckBox rememberMeCheckbox = new CheckBox("Remember Me");
        Button loginButton = new Button("Login");

        loginButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        loginButton.addClickListener(event -> {
            if (userMgr.loginSuper(emailField.getValue(), passwordField.getValue()) != null)
            {
                if (rememberMeCheckbox.getValue())
                {
                    UserToken userToken = userMgr.createToken();
                    VaadinSessionManager.setUserCookie(userToken.getId());
                }
                else
                {
                    // todo - this should be somewhere else
                    VaadinSessionManager.clearUserCookie();
                }

                if (sessionMgr.hasSuperUser()) { getUI().getNavigator().navigateTo(SuperAdminPage.NAME); }
                else { Notification.show("User not a super user", Notification.Type.ERROR_MESSAGE); }
            }
            else
            {
                Notification.show("Invalid email/password", Notification.Type.ERROR_MESSAGE);
            }
        });

        layout.addComponents(emailField, passwordField, rememberMeCheckbox, loginButton);
    }
}
