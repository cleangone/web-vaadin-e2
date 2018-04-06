package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.person.Person;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.aws.dynamo.entity.person.UserToken;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.EntityChangeManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.MyUI;
import xyz.cleangone.e2.web.vaadin.desktop.admin.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.org.disclosure.BaseDisclosure;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;
import xyz.cleangone.message.EmailSender;

import static java.util.Objects.requireNonNull;
import static xyz.cleangone.data.aws.dynamo.entity.person.User.*;
import static xyz.cleangone.e2.web.vaadin.util.DisclosureUtils.createCheckBox;
import static xyz.cleangone.e2.web.vaadin.util.DisclosureUtils.createTextField;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class ProfileAdmin extends BaseAdmin
{
    private final FormLayout formLayout = new FormLayout();
    private EmailSender emailSender = new EmailSender();

    private SessionManager sessionMgr;
    private UserManager userMgr;
    private Organization org;
    private User user;
    private Person person;
    private EntityChangeManager changeManager = new EntityChangeManager();

    public ProfileAdmin(MessageDisplayer msgDisplayer)
    {
        super(msgDisplayer);

        setMargin(false);
        setSpacing(false);

        formLayout.setMargin(true);
        formLayout.setSpacing(false);
    }

    public void set(SessionManager sessionMgr)
    {
        this.sessionMgr = sessionMgr;
        userMgr = sessionMgr.getPopulatedUserManager();
        org = sessionMgr.getOrg();
        user = userMgr.getUser();
        person = userMgr.getPerson();

        set();
    }

    public void set()
    {
        if (changeManager.unchanged(user) &&
            changeManager.unchangedEntity(user.getId()) &&
            changeManager.unchangedEntity(user.getPersonId()))
        {
            return;
        }

        changeManager.reset(user);
        removeAllComponents();
        formLayout.removeAllComponents();

        formLayout.addComponent(new NameDisclosure());
        formLayout.addComponent(new EmailDisclosure());
        formLayout.addComponent(new PhoneDisclosure());
        formLayout.addComponent(new AddressDisclosure());
        formLayout.addComponent(new PasswordDisclosure());

        addComponents(formLayout);
        setExpandRatio(formLayout, 1.0f);
    }

    class NameDisclosure extends BaseDisclosure
    {
        NameDisclosure()
        {
            super("Name", new FormLayout());
            setDisclosureCaption();

            mainLayout.addComponents(
                createPersonTextField(Person.FIRST_NAME_FIELD, this),
                createPersonTextField(Person.LAST_NAME_FIELD, this));
        }

        public void setDisclosureCaption()
        {
            String caption = StringUtils.isBlank(person.getFirstLast()) ? "Name not set" : person.getFirstLast();
            setDisclosureCaption(caption);
        }
    }

    class EmailDisclosure extends BaseDisclosure
    {
        EmailDisclosure()
        {
            super("Email", new HorizontalLayout());
            setDisclosureCaption();

            boolean emailVerified = user.getEmailVerified();
            Label emailVerifiedLabel = new Label(emailVerified ? "Verified" : "Not Verified");

            Button verifyEmailButton = new Button("Verify");
            verifyEmailButton.addStyleName(ValoTheme.BUTTON_SMALL);
            verifyEmailButton.addClickListener(e -> sendVerificationEmail());

            TextField emailField = VaadinUtils.createTextField(null, user.getEmail());
            emailField.addValueChangeListener(event -> {
                String newEmail = event.getValue();
                if (userMgr.getUserWithEmail(newEmail, org.getId()) == null)
                {
                    user.setEmail(newEmail);
                    user.setEmailVerified(false);
                    userMgr.getUserDao().save(user);

                    if (emailVerified)
                    {
                        // email changed from verified to not
                        emailVerifiedLabel.setValue("Not Verified");
                        mainLayout.addComponent(verifyEmailButton);
                    }

                    msgDisplayer.displayMessage("Email saved");
                    setDisclosureCaption();
                }
                else
                {
                    Notification.show("A user with email '" + newEmail + "' already exists", Notification.Type.ERROR_MESSAGE);
                    emailField.setValue(user.getEmail());
                }
            });

            mainLayout.addComponents(emailField, emailVerifiedLabel);
            if (!emailVerified) { mainLayout.addComponent(verifyEmailButton); }
            mainLayout.setComponentAlignment(emailVerifiedLabel, new Alignment(AlignmentInfo.Bits.ALIGNMENT_VERTICAL_CENTER));
        }

        void sendVerificationEmail()
        {
            String userEmail = user.getEmail();
            if (StringUtils.isBlank(userEmail)) { return; }

            UserToken token = userMgr.createToken();
            String link = sessionMgr.getUrl(MyUI.VERIFY_EMAIL_URL_PARAM, token);
            String subject = "Email Verification";

            String htmlBody = "<h1>Please Verify Email</h1> " +
                "<p>Verify your email by clicking the following link or pasting it in your browser.</p> " +
                "<p><a href='" + link + "'>" + link + "</a>";

            String textBody = "Verify your email by pasting the following link into your browwser: " + link;

            boolean emailSent = emailSender.sendEmail(userEmail, subject, htmlBody, textBody);
            msgDisplayer.displayMessage(emailSent ? "Verification email sent" : "Error sending verification email");
        }

        public void setDisclosureCaption()
        {
            String caption = user.getEmail() == null ? "Email not set" :
                user.getEmail() + " (" + (user.getEmailVerified() ? "" : "Not ") + "Verified)";
            setDisclosureCaption(caption);
        }
    }

    class PhoneDisclosure extends BaseDisclosure
    {
        PhoneDisclosure()
        {
            super("Phone", new FormLayout());

            setDisclosureCaption();

            mainLayout.addComponents(
                createUserTextField(PHONE_FIELD, this),
                createCheckBox(ACCEPT_TEXTS_FIELD, user, userMgr.getUserDao(), msgDisplayer, this));
        }

        public void setDisclosureCaption()
        {
            String caption = (user.getPhone() == null) ? "Phone not set" :
                getPhoneFriendly() + (user.getAcceptTexts() ? " (Accepts texts)" : " (Does not accept texts)");

            setDisclosureCaption(caption);
        }

        private String getPhoneFriendly()
        {
            String phone = user.getPhone();
            if (phone == null) return "";
            if (phone.length() == 7) return phone.substring(0,3) + "-" + phone.substring(3);
            if (phone.length() == 10) return phone.substring(0,3) + "-" + phone.substring(3,6) + "-" + phone.substring(6);
            return phone;
        }
    }

    class AddressDisclosure extends BaseDisclosure
    {
        AddressDisclosure()
        {
            super("Address", new FormLayout());

            setDisclosureCaption();

            mainLayout.addComponents(
                createUserTextField(ADDRESS_FIELD, this),
                createUserTextField(CITY_FIELD, this),
                createUserTextField(STATE_FIELD, this),
                createUserTextField(ZIP_FIELD, this));
        }

        public void setDisclosureCaption()
        {
            String caption =
                (user.getAddress() == null && user.getCity() == null && user.getState() == null && user.getZip() == null) ?
                    "Address not set" :
                    getOrDefault(user.getAddress(),   "Address") + ", " +
                        getOrDefault(user.getCity(),  "City")    + ", " +
                        getOrDefault(user.getState(), "State")   + ", " +
                        getOrDefault(user.getZip(),   "Zip");

            setDisclosureCaption(caption);
        }

        String getOrDefault(String value, String defaultValue) { return value == null ? "<No " + defaultValue + ">" : value; }
    }

    class PasswordDisclosure extends BaseDisclosure
    {
        PasswordDisclosure()
        {
            super("Password", new FormLayout());

            setDisclosureCaption();

            PasswordField currPasswordField = new PasswordField("Current Password");
            PasswordField newPasswordField = new PasswordField("New Password");
            PasswordField confirmField = new PasswordField("Confirm Password");

            Button button = new Button("Update Password");
            button.addStyleName(ValoTheme.BUTTON_SMALL);
            button.addClickListener(event -> {
                if (!userMgr.passwordMatches(currPasswordField.getValue())) { showError("Current Password not correct"); }
                else if (newPasswordField.getValue().isEmpty()) { showError("New Password not set"); }
                else if (!newPasswordField.getValue().equals(confirmField.getValue())) { showError("Password and Confirm do not match"); }
                else
                {
                    user.setPassword(newPasswordField.getValue());
                    userMgr.getUserDao().save(user);

                    currPasswordField.clear();
                    newPasswordField.clear();
                    confirmField.clear();
                    setDisclosureCaption();
                    msgDisplayer.displayMessage("Password updated");
                }
            });

            mainLayout.addComponents(currPasswordField, newPasswordField, confirmField, button);
        }

        public void setDisclosureCaption()
        {
            setDisclosureCaption("Password " + (user.getEncryptedPassword()==null ? " not" : "") + " set");
        }
    }

    private TextField createUserTextField(EntityField field, BaseDisclosure disclosure)
    {
        return createTextField(field, user, userMgr.getUserDao(), msgDisplayer, disclosure);
    }

    private TextField createPersonTextField(EntityField field, BaseDisclosure disclosure)
    {
        return createTextField(field, person, userMgr.getPersonDao(), msgDisplayer, disclosure);
    }


}