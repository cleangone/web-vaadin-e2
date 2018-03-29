package xyz.cleangone.e2.web.vaadin.desktop.org;

import static xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity.CREATED_DATE_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.person.User.*;
import static xyz.cleangone.e2.web.vaadin.util.DisclosureUtils.*;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import xyz.cleangone.data.aws.dynamo.dao.DynamoBaseDao;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.person.Person;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.aws.dynamo.entity.person.UserToken;
import xyz.cleangone.data.manager.ActionManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.MyUI;
import xyz.cleangone.e2.web.vaadin.desktop.admin.org.disclosure.BaseDisclosure;

import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;
import xyz.cleangone.message.EmailSender;

import java.text.DateFormat;
import java.util.List;

public class ProfilePage extends BaseOrgPage implements View
{
    public static final String NAME = "UserProfile";
    public static final String DISPLAY_NAME = "User Profile";

    private FormLayout topLayout = new FormLayout();
    private VerticalLayout bottomLayout = new VerticalLayout();
    private EmailSender emailSender = new EmailSender();

    private UserManager userMgr;
    private ActionManager actionMgr;
    private Organization org;
    private User user;
    private Person person;

    public ProfilePage()
    {
        topLayout.setMargin(true);
        topLayout.setSpacing(false);
        mainLayout.addComponents(topLayout, bottomLayout);
    }

    protected void set(SessionManager sessionMgr)
    {
        sessionMgr.resetEventManager();
        super.set(sessionMgr);
    }

    protected void set()
    {
        topLayout.removeAllComponents();
        bottomLayout.removeAllComponents();

        userMgr = sessionMgr.getPopulatedUserManager();
        actionMgr = sessionMgr.getOrgManager().getActionManager();
        org = sessionMgr.getOrg();
        user = userMgr.getUser();
        person = userMgr.getPerson();

        topLayout.addComponent(new NameDisclosure());
        topLayout.addComponent(new EmailDisclosure());
        topLayout.addComponent(new PhoneDisclosure());
        topLayout.addComponent(new AddressDisclosure());
        topLayout.addComponent(new PasswordDisclosure());

        bottomLayout.addComponent(getActionGrid(user));
    }

    private Grid<Action> getActionGrid(User user)
    {
        Grid<Action> grid = new Grid<>();
        grid.setSizeFull();

        grid.addColumn(Action::getCreatedDate).setCaption("Date")
            .setId(CREATED_DATE_FIELD.getName())
            .setRenderer(new DateRenderer(DateFormat.getDateInstance(DateFormat.MEDIUM)));
        grid.addColumn(Action::getTargetEventName).setCaption("Event");
        grid.addColumn(Action::getActionType).setCaption("Action");
        grid.addColumn(Action::getDisplayAmount).setCaption("Amount");
        grid.addColumn(Action::getDescription).setCaption("Description");
        grid.addColumn(Action::getTargetPersonFirstLast).setCaption("For Person");

        grid.sort(CREATED_DATE_FIELD.getName(), SortDirection.DESCENDING);

        List<Action> actions = actionMgr.getActionsBySourcePerson(user.getPersonId());
        grid.setDataProvider(new ListDataProvider<>(actions));

        return grid;
    }

    private TextField createUserTextField(EntityField field, BaseDisclosure disclosure)
    {
        return createTextField(field, user, userMgr.getUserDao(), actionBar, disclosure);
    }

    private TextField createPersonTextField(EntityField field, BaseDisclosure disclosure)
    {
        return createTextField(field, person, userMgr.getPersonDao(), actionBar, disclosure);
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

                    actionBar.displayMessage("Email saved");
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
            actionBar.displayMessage(emailSent ? "Verification email sent" : "Error sending verification email");
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
                createCheckBox(ACCEPT_TEXTS_FIELD, user, userMgr.getUserDao(), actionBar, this));
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
                    actionBar.displayMessage("Password updated");
                }
            });

            mainLayout.addComponents(currPasswordField, newPasswordField, confirmField, button);
        }

        public void setDisclosureCaption()
        {
            setDisclosureCaption("Password " + (user.getEncryptedPassword()==null ? " not" : "") + " set");
        }
    }
}
