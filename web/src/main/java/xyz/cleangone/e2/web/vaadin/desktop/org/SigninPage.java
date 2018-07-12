package xyz.cleangone.e2.web.vaadin.desktop.org;

import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.ui.*;

import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.aws.dynamo.entity.person.UserToken;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.web.vaadin.ui.PageDisplayType;
import xyz.cleangone.web.vaadin.util.VaadinUtils;

import static xyz.cleangone.e2.web.manager.CookieManager.*;
import static xyz.cleangone.web.vaadin.util.VaadinUtils.*;

public class SigninPage extends BaseOrgPage implements View
{
    public static final String NAME = "SignIn";
    public static final String DISPLAY_NAME = "Sign In";

    private UserManager userMgr;

    protected PageDisplayType set()
    {
        userMgr = sessionMgr.getUserManager();

        mainLayout.removeAllComponents();
        mainLayout.addComponent(getLogin());

        return PageDisplayType.NotApplicable;
    }

    private Component getLogin()
    {
        FormLayout layout = formLayout(MARGIN_TRUE, SPACING_TRUE, VaadinUtils.SIZE_UNDEFINED);

        TextField emailField = new TextField("Email");
        PasswordField passwordField = new PasswordField("Password");
        CheckBox rememberMeCheckbox = new CheckBox("Remember Me");

        Button loginButton = new Button("Login");
        loginButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        loginButton.addClickListener(e -> login(emailField.getValue(), passwordField.getValue(), rememberMeCheckbox.getValue()));

        layout.addComponents(emailField, passwordField, rememberMeCheckbox, loginButton);
        return layout;
    }

    private void login(String email, String password, boolean rememberMe)
    {
        User user = userMgr.login(email, password);
        if (user == null)
        {
            Notification.show("Invalid email/password", Notification.Type.ERROR_MESSAGE);
            return;
        }

        boolean orgAdded = user.addOrgId(sessionMgr.getOrgId());
        if (orgAdded) { userMgr.saveUser(); }

        if (rememberMe)
        {
            UserToken userToken = userMgr.cycleToken();
            setUserCookie(userToken.getId());
        }
        else
        {
            // todo - this should be somewhere else
            clearUserCookie();
        }

        String navToPage = sessionMgr.getNavToAfterLogin(OrgPage.NAME);
        getUI().getNavigator().navigateTo(navToPage);
    }

}