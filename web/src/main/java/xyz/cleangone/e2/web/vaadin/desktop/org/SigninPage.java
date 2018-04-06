package xyz.cleangone.e2.web.vaadin.desktop.org;

import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.person.UserToken;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.VaadinSessionManager;


public class SigninPage extends BaseOrgPage implements View
{
    public static final String NAME = "SignIn";
    public static final String DISPLAY_NAME = "Sign In";

    protected PageDisplayType set()
    {
        mainLayout.removeAllComponents();
        mainLayout.addComponent(getLogin());

        return PageDisplayType.NotApplicable;
    }

    private Component getLogin()
    {
        String navToPage = sessionMgr.getNavToAfterLogin(OrgPage.NAME);

        FormLayout layout = new FormLayout();
        layout.setSizeUndefined();
        layout.setMargin(true);
        layout.setSpacing(true);

        TextField emailField = new TextField("Email");
        PasswordField passwordField = new PasswordField("Password");
        CheckBox rememberMeCheckbox = new CheckBox("Remember Me");

        Button loginButton = new Button("Login");
        loginButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        loginButton.addClickListener(new Button.ClickListener()
        {
            @Override
            public void buttonClick(Button.ClickEvent event)
            {
                UserManager userMgr = sessionMgr.getUserManager();
                if (userMgr.login(emailField.getValue(), passwordField.getValue(), sessionMgr.getOrg()) != null)
                {
                    if (rememberMeCheckbox.getValue())
                    {
                        UserToken userToken = userMgr.cycleToken();
                        VaadinSessionManager.setUserCookie(userToken.getId());
                    }
                    else
                    {
                        // todo - this should be somewhere else
                        VaadinSessionManager.clearUserCookie();
                    }

                    getUI().getNavigator().navigateTo(navToPage);
                }
                else
                {
                    Notification.show("Invalid email/password", Notification.Type.ERROR_MESSAGE);
                }
            }
        });

        layout.addComponents(emailField, passwordField, rememberMeCheckbox, loginButton);

        return layout;
    }
}