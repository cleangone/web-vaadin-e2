package xyz.cleangone.e2.web.vaadin.desktop.org;

import com.vaadin.navigator.View;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.web.vaadin.ui.PageDisplayType;

public class PasswordResetPage extends BaseOrgPage implements View
{
    public static final String NAME = "ResetPassword";

    private UserManager userMgr;

    public PasswordResetPage()
    {
        mainLayout.setMargin(true);
        mainLayout.setWidth("100%");
    }

    protected PageDisplayType set()
    {
        userMgr = sessionMgr.getUserManager();

        mainLayout.removeAllComponents();
        Component passwordPanel = getPasswordPanel();
        mainLayout.addComponent(passwordPanel);
        mainLayout.setComponentAlignment(passwordPanel, Alignment.MIDDLE_CENTER);

        return PageDisplayType.NotApplicable;
    }

    private Component getPasswordPanel()
    {
        Panel panel = new Panel("Reset Password");
        panel.setSizeUndefined();

        FormLayout layout = new FormLayout();
        panel.setContent(layout);

        layout.setSizeFull();
        layout.setMargin(true);
        layout.setSpacing(true);

        PasswordField newPasswordField = new PasswordField("New Password");
        layout.addComponent(newPasswordField);

        PasswordField confirmField = new PasswordField("Confirm Password");
        layout.addComponent(confirmField);

        Button button = new Button("Update Password");
        button.addStyleName(ValoTheme.BUTTON_SMALL);
        layout.addComponent(button);
        button.addClickListener(event -> {
            if (newPasswordField.getValue().isEmpty()) { showError("New Password not set"); }
            else if (!newPasswordField.getValue().equals(confirmField.getValue())) { showError("Password and Confirm do not match"); }
            else
            {
                User user = userMgr.getUser();
                user.setPassword(newPasswordField.getValue());
                userMgr.getUserDao().save(user);

                // delete the password reset token
                userMgr.deleteToken();

                newPasswordField.clear();
                confirmField.clear();
                actionBar.displayMessage("Password updated");
            }
        });

        return panel;
    }
}