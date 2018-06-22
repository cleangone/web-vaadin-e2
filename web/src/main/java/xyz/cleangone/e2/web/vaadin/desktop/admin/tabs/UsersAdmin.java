package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs;

import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.person.AdminPrivledge;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.vaadin.util.CountingDataProvider;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.MultiFieldFilter;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.List;
import java.util.stream.Collectors;

import static xyz.cleangone.data.aws.dynamo.entity.person.User.*;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class UsersAdmin extends VerticalLayout
{
    private final MessageDisplayer msgDisplayer;
    private OrgManager orgMgr;
    private UserManager userMgr;
    private String orgId;

    public UsersAdmin(MessageDisplayer msgDisplayer)
    {
        this.msgDisplayer = msgDisplayer;
        setLayout(this, MARGIN_TRUE, SPACING_TRUE, SIZE_FULL, BACK_BLUE);
    }

    public void set(OrgManager orgMgr, UserManager userMgr)
    {
        this.orgMgr = orgMgr;
        this.userMgr = userMgr;
        orgId = orgMgr.getOrgId();

        set();
    }

    public void set()
    {
        removeAllComponents();

        Component grid = new UserGrid();
        addComponents(getAddUserLayout(), grid);
        setExpandRatio(grid, 1.0f);
    }

    private Component getAddUserLayout()
    {
        HorizontalLayout layout = horizontal(VaadinUtils.SIZE_UNDEFINED);

        TextField firstNameField = VaadinUtils.createGridTextField("First Name");
        TextField lastNameField = VaadinUtils.createGridTextField("Last Name");
        TextField emailField = VaadinUtils.createGridTextField("Email");

        Button button = new Button("Add User");
        button.addStyleName(ValoTheme.TEXTFIELD_TINY);
        button.addClickListener(event -> {
            String firstName = firstNameField.getValue();
            String lastName = lastNameField.getValue();
            String email = emailField.getValue();

            if (firstName.length() == 0) { showError("First Name required"); }
            else if (lastName.length() == 0) { showError("Last Name required"); }
            else if (email.length() == 0) { showError("Email required"); }
            else if (userMgr.emailExists(email)) { showError("Email already exists"); }
            else
            {
                userMgr.createUser(email, firstName, lastName, orgMgr.getOrgId());
                set();
            }
        });

        layout.addComponents(firstNameField, lastNameField, emailField, button);
        return layout;
    }

    private class UserGrid extends BaseGrid<User>
    {
        UserGrid()
        {
            setSizeFull();

            Grid.Column<User, String> nameCol = addColumn(LAST_FIRST_FIELD, User::getLastCommaFirst);

            // cannot edit email - should you be able to edit email just created?  or delete & recreate?
            addColumn(EMAIL_FIELD, User::getEmail);
            addColumn(PASSWORD_FIELD, User::getPassword, User::setPassword);  // shows blank, clear text when typed in
            addColumn(this::isOrgAdmin)
                .setId(ADMIN_FIELD.getName()).setCaption(ADMIN_FIELD.getDisplayName())
                .setEditorComponent(new CheckBox(), this::setOrgAdmin);
            addComponentColumn(this::buildDeleteButton).setWidth(ICON_COL_WIDTH);

            sort(nameCol, SortDirection.ASCENDING);

            getEditor().setEnabled(true);
            getEditor().addSaveListener(event -> {
                User user = event.getBean();
                userMgr.save(user);
                msgDisplayer.displayMessage("User updates saved");
                set();
            });

            List<User> users = orgMgr.getUsers().stream()
                .filter(user -> !user.isSuperAdmin())
                .collect(Collectors.toList());

            CountingDataProvider<User> dataProvider = new CountingDataProvider<>(users, countLabel);
            setDataProvider(dataProvider);

            HeaderRow filterHeader = appendHeaderRow();
            setColumnFiltering(filterHeader, dataProvider);

            appendCountFooterRow(LAST_FIRST_FIELD);
        }

        private boolean isOrgAdmin(User user)
        {
            return user.isOrgAdmin(orgId);
        }
        private void setOrgAdmin(User user, boolean isAdmin)
        {
            if (isAdmin) { user.addAdminPrivledge(new AdminPrivledge(orgId)); }
            else { user.removeAdminPrivledge(new AdminPrivledge(orgId)); }
        }

        private Button buildDeleteButton(User user)
        {
            // can only delete users that have not had password set - ie. just been created
            return (user.hasPassword() ? null :
                buildDeleteButton(user, "Delete User", "Confirm User Delete", "Delete user '" + user.getFirstLast() + "'?"));
        }

        @Override
        protected void delete(User user)
        {
            userMgr.delete(user);
            set();
        }

        private void setColumnFiltering(HeaderRow filterHeader, CountingDataProvider<User> dataProvider)
        {
            MultiFieldFilter<User> filter = new MultiFieldFilter<>(dataProvider);

            addFilterField(LAST_FIRST_FIELD, User::getLastCommaFirst, filter, filterHeader);
            addFilterField(EMAIL_FIELD, User::getEmail, filter, filterHeader);
        }
    }
}

