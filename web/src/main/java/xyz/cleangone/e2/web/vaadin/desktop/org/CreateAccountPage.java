package xyz.cleangone.e2.web.vaadin.desktop.org;

import com.vaadin.navigator.View;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.person.Person;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;


public class CreateAccountPage extends BaseOrgPage implements View
{
    public static final String NAME = "CreateAccount";
    public static final String DISPLAY_NAME = "Create Account";

    protected void set()
    {
        mainLayout.removeAllComponents();

        UserManager userMgr = sessionMgr.getUserManager();
        Organization org = sessionMgr.getOrg();

        User user = new User();
        Person person = new Person();

        FormLayout formLayout = new FormLayout();
        formLayout.setMargin(new MarginInfo(true));
        mainLayout.addComponent(formLayout);

        formLayout.addComponent(createTextField(User.EMAIL_FIELD, user));
        formLayout.addComponent(createTextField(Person.FIRST_NAME_FIELD, person));
        formLayout.addComponent(createTextField(Person.LAST_NAME_FIELD, person));

        PasswordField passwordField = new PasswordField("Password");
        formLayout.addComponent(passwordField);

        PasswordField confirmField = new PasswordField("Confirm Password");
        formLayout.addComponent(confirmField);

        Button button = createTextButton("Create Account");
        formLayout.addComponent(button);
        button.addClickListener(event -> {
            if (user.getEmail() == null) { showError("Email not set"); }
            else if (passwordField.getValue().isEmpty()) { showError("Password not set"); }
            else if (!passwordField.getValue().equals(confirmField.getValue())) { showError("Password and Confirm do not match"); }
            else if (userMgr.emailExists(user.getName(), org.getId())) { showError("Email already exists"); }
            else
            {
                person.setOrgId(org.getId());
                userMgr.getPersonDao().save(person);

                user.setPersonId(person.getId());
                user.setOrgId(org.getId());
                user.setPassword(passwordField.getValue());
                userMgr.getUserDao().save(user);

                userMgr.setUser(user);
                getUI().getNavigator().navigateTo(OrgPage.NAME);

                // todo - setSessionMsg("User created"), which will be displayed by next ActionBar
            }
        });
    }

    private TextField createTextField(EntityField field, BaseEntity entity)
    {
        TextField textField = VaadinUtils.createTextField(field.getDisplayName(), entity.get(field), null);
        textField.addValueChangeListener(event -> entity.set(field, event.getValue()));

        return textField;
    }
}
