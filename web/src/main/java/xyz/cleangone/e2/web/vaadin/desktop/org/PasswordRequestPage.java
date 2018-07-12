package xyz.cleangone.e2.web.vaadin.desktop.org;

import com.vaadin.navigator.View;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import org.apache.commons.lang3.StringUtils;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.aws.dynamo.entity.person.UserToken;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.vaadin.desktop.MyUI;
import xyz.cleangone.web.vaadin.util.VaadinUtils;
import xyz.cleangone.message.EmailSender;

import static xyz.cleangone.web.vaadin.util.VaadinUtils.*;

public class PasswordRequestPage extends BaseOrgPage implements View
{
    public static final String NAME = "PasswordRequest";
    public static final String DISPLAY_NAME = "Request Reset Password";

    private final EmailSender emailSender = new EmailSender();

    private UserManager userMgr;
    private Organization org;


    protected PageDisplayType set()
    {
        userMgr = sessionMgr.getUserManager();
        org = sessionMgr.getOrg();

        mainLayout.removeAllComponents();
        mainLayout.addComponent(getSendEmail());

        return PageDisplayType.NotApplicable;
    }

    private Component getSendEmail()
    {
        FormLayout layout = formLayout(MARGIN_TRUE, SPACING_TRUE, VaadinUtils.SIZE_UNDEFINED);

        TextField emailField = new TextField("Email");

        Button sendEmailButton = new Button("Send reset email");
        sendEmailButton.addClickListener(event -> {
            String email = emailField.getValue();
            if (StringUtils.isBlank(email))
            {
                showError("Email not set");
                return;
            }

            User user = userMgr.getUserWithEmail(email);
            if (user == null)
            {
                // todo - silently fail instead?
                showError("User with email '" + emailField.getValue() + "' does not exist");
            }
            else
            {
                boolean emailSent = sendResetEmail(user);
                actionBar.displayMessage(emailSent ? "Password Reset email sent" : "Error sending Password Reset email");
                if (emailSent) { emailField.setValue(""); }
            }
        });

        layout.addComponents(emailField, sendEmailButton);

        return layout;
    }

    private boolean sendResetEmail(User user)
    {
        UserToken token = userMgr.createToken(user);
        String link = sessionMgr.getUrl(MyUI.RESET_PASSWORD_URL_PARAM, token);

        String subject = "Password Reset";

        String htmlBody = "<h1>Password Reset</h1> " +
            "<p>Reset your password by clicking the following link or pasting it in your browser.</p> " +
            "<p><a href='" + link + "'>" + link + "</a>";

        String textBody = "Reset your password by pasting the following link into your browwser: " + link;

        return emailSender.sendEmail(user.getEmail(), subject, htmlBody, textBody);
    }
}