package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.person.Address;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.aws.dynamo.entity.person.UserToken;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.EntityChangeManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.MyUI;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.util.disclosure.BaseDisclosure;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;
import xyz.cleangone.message.EmailSender;

import static java.util.Objects.requireNonNull;
import static xyz.cleangone.data.aws.dynamo.entity.person.User.*;
import static xyz.cleangone.data.aws.dynamo.entity.person.Address.*;
import static xyz.cleangone.e2.web.vaadin.util.disclosure.DisclosureUtils.createCheckBox;
import static xyz.cleangone.e2.web.vaadin.util.disclosure.DisclosureUtils.createTextField;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class ProfileAdmin extends BaseAdmin
{
    protected final FormLayout formLayout = formLayout(MARGIN_TRUE, SPACING_FALSE);
    private EmailSender emailSender = new EmailSender();

    private SessionManager sessionMgr;
    private UserManager userMgr;
    private Organization org;
    private User user;
    private Address address;
    private EntityChangeManager changeManager = new EntityChangeManager();

    public ProfileAdmin(MessageDisplayer msgDisplayer)
    {
        super(msgDisplayer);
        setLayout(this, MARGIN_FALSE, SPACING_FALSE);
    }

    public void set(SessionManager sessionMgr)
    {
        this.sessionMgr = sessionMgr;
        userMgr = sessionMgr.getPopulatedUserManager();
        org = sessionMgr.getOrg();
        user = userMgr.getUser();
        address = userMgr.getAddress();

        set();
    }

    public void set()
    {
        if (changeManager.unchanged(user) &&
            changeManager.unchangedEntity(user.getId()))
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
        formLayout.addComponent(new BiddingDisclosure());

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
                createUserTextField(User.FIRST_NAME_FIELD, this),
                createUserTextField(User.LAST_NAME_FIELD, this));
        }

        public void setDisclosureCaption()
        {
            String caption = StringUtils.isBlank(user.getFirstLast()) ? "Name not set" : user.getFirstLast();
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
                if (!userMgr.emailExists(newEmail))
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

            if (address != null) { setFields(); }
            setDisclosureCaption();
        }

        // called when opened
        public void setOpen(boolean open)
        {
            super.setOpen(open);

            if (address == null)
            {
                address = userMgr.createAddress();
                setFields();
            }
        }

        void setFields()
        {
            mainLayout.addComponents(
                createAddressTextField(ADDRESS_FIELD, this),
                createAddressTextField(CITY_FIELD, this),
                createAddressTextField(STATE_FIELD, this),
                createAddressTextField(ZIP_FIELD, this));
        }

        public void setDisclosureCaption()
        {
            setDisclosureCaption(address == null ? "Address not set" :
                getOrDefault(Address.ADDRESS_FIELD) + ", " +
                getOrDefault(Address.CITY_FIELD)    + ", " +
                getOrDefault(Address.STATE_FIELD)   + ", " +
                getOrDefault(Address.ZIP_FIELD));
        }

        String getOrDefault(EntityField field) { return address.get(field) == null ? "<No " + field.getDisplayName() + ">" : address.get(field); }
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

    class BiddingDisclosure extends BaseDisclosure
    {
        BiddingDisclosure()
        {
            super("Bidding", new FormLayout());

            setDisclosureCaption();
            mainLayout.addComponents(
                createCheckBox(SHOW_BID_CONFIRM_FIELD, user, userMgr.getUserDao(), msgDisplayer, this),
                createCheckBox(SHOW_QUICK_BID_FIELD,   user, userMgr.getUserDao(), msgDisplayer, this));
        }

        public void setDisclosureCaption()
        {
            setDisclosureCaption(
                "Bid Confirmation " + (user.getShowBidConfirm() ? "on" : "off") +
                ", Quick-Bid Button " + (user.getShowQuickBid() ? "displayed" : "not displayed"));
        }
    }

    private TextField createUserTextField(EntityField field, BaseDisclosure disclosure)
    {
        return createTextField(field, user, userMgr.getUserDao(), msgDisplayer, disclosure);
    }

//    private TextField createPersonTextField(EntityField field, BaseDisclosure disclosure)
//    {
//        return createTextField(field, person, userMgr.getPersonDao(), msgDisplayer, disclosure);
//    }

    private TextField createAddressTextField(EntityField field, BaseDisclosure disclosure)
    {
        return createTextField(field, address, userMgr.getAddressDao(), msgDisplayer, disclosure);
    }

}