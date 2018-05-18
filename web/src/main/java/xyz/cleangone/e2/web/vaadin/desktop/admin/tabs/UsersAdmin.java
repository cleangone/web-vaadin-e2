package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.Setter;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.FooterRow;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.person.AdminPrivledge;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.vaadin.util.CountingDataProvider;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
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

        Component grid = getUserGrid();
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

    private Component getUserGrid()
    {
        Grid<User> grid = new Grid<>();
        grid.setSizeFull();

        Grid.Column<User, String> nameCol = addColumn(grid, LAST_FIRST_FIELD, User::getLastCommaFirst);

        // cannot edit email - should you be able to edit email just created?  or delete & recreate?
        addColumn(grid, EMAIL_FIELD, User::getEmail);
        addColumn(grid, PASSWORD_FIELD, User::getPassword, User::setPassword);  // shows blank, clear text when typed in
        grid.addColumn(this::isOrgAdmin)
            .setId(ADMIN_FIELD.getName()).setCaption(ADMIN_FIELD.getDisplayName())
            .setEditorComponent(new CheckBox(), this::setOrgAdmin);

        // todo - cannot disable user - should admin be able to block them?
        // addBooleanColumn(grid, ENABLED_FIELD, User::getEnabled, User::setEnabled);

        grid.addComponentColumn(this::buildDeleteButton);

        grid.sort(nameCol, SortDirection.ASCENDING);

        grid.getEditor().setEnabled(true);
        grid.getEditor().addSaveListener(event -> {
            User user = event.getBean();
            userMgr.save(user);
            msgDisplayer.displayMessage("User updates saved");
            set();
        });

        List<User> users = orgMgr.getUsers().stream()
            .filter(user -> !user.isSuperAdmin())
            .collect(Collectors.toList());

        Label countLabel = new Label();
        CountingDataProvider<User> dataProvider = new CountingDataProvider<User>(users, countLabel);
        grid.setDataProvider(dataProvider);

        HeaderRow filterHeader = grid.appendHeaderRow();
        setColumnFiltering(filterHeader, dataProvider);

        FooterRow footerRow = grid.appendFooterRow();
        footerRow.getCell(EMAIL_FIELD.getName()).setComponent(countLabel);

        return grid;
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

    private Grid.Column<User, String> addColumn(
        Grid<User> grid, EntityField entityField, ValueProvider<User, String> valueProvider, Setter<User, String> setter)
    {
        return addColumn(grid, entityField, valueProvider)
            .setEditorComponent(new TextField(), setter);
    }

    private Grid.Column<User, String> addColumn(
        Grid<User> grid, EntityField entityField, ValueProvider<User, String> valueProvider)
    {
        return grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName());
    }

    private void addBooleanColumn(
        Grid<User> grid, EntityField entityField, ValueProvider<User, Boolean> valueProvider, Setter<User, Boolean> setter)
    {
        grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName())
            .setEditorComponent(new CheckBox(), setter);
    }

    // can only delete users that have not had password set - ie. just been created
    private Button buildDeleteButton(User user)
    {
        if (user.hasPassword()) { return null; }

        Button button = createDeleteButton("Delete User");
        button.addClickListener(e -> {
            ConfirmDialog.show(getUI(), "Confirm User Delete", "Delete user '" + user.getName() + "'?",
                "Delete", "Cancel", new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            userMgr.delete(user);
                            set();
                        }
                    }
                });
        });

        return button;
    }

    private void setColumnFiltering(HeaderRow filterHeader, ListDataProvider<User> dataProvider)
    {
        addFilterField(EMAIL_FIELD, User::getName, dataProvider, filterHeader);
        addFilterField(LAST_FIRST_FIELD, User::getLastCommaFirst, dataProvider, filterHeader);
    }

    private void addFilterField(
        EntityField entityField, ValueProvider<User, String> valueProvider, ListDataProvider<User> dataProvider, HeaderRow filterHeader)
    {
        TextField filterField = VaadinUtils.createGridTextField("Filter");
        filterField.addValueChangeListener(event -> {
            dataProvider.setFilter(valueProvider, s -> contains(s, event.getValue()));
        });

        filterHeader.getCell(entityField.getName()).setComponent(filterField);
    }

    private boolean contains(String s, String contains)
    {
        return (s != null && contains != null && s.toLowerCase().contains(contains.toLowerCase()));
    }
}

