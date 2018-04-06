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
import xyz.cleangone.data.aws.dynamo.entity.person.Person;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.vaadin.util.CountingDataProvider;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import static xyz.cleangone.data.aws.dynamo.entity.person.User.*;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class UsersAdmin extends VerticalLayout
{
    private final MessageDisplayer msgDisplayer;
    private OrgManager orgMgr;
    private UserManager userMgr;


    public UsersAdmin(MessageDisplayer msgDisplayer)
    {
        this.msgDisplayer = msgDisplayer;

        setSizeFull();
        setSpacing(true);
    }

    public void set(OrgManager orgMgr, UserManager userMgr)
    {
        this.orgMgr = orgMgr;
        this.userMgr = userMgr;

        set();
    }

    public void set()
    {
        removeAllComponents();

        // todo - sort people
        List<Person> people = orgMgr.getPeople();
        Map<String, Person> peopleById = people.stream()
            .collect(Collectors.toMap(Person::getId, Function.identity()));

        addComponent(getAddUserLayout(people));

        Component grid = getUserGrid(peopleById);
        addComponent(grid);
        setExpandRatio(grid, 1.0f);
    }

    private Component getAddUserLayout(List<Person> people)
    {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeUndefined();

        ComboBox<Person> personComboBox = new ComboBox<>();
        layout.addComponent(personComboBox);
        personComboBox.addStyleName(ValoTheme.TEXTFIELD_TINY);
        personComboBox.setPlaceholder("Person");
        personComboBox.setItemCaptionGenerator(Person::getLastCommaFirst);
        personComboBox.setItems(people);

        TextField emailField = VaadinUtils.createGridTextField("Email");
        layout.addComponent(emailField);

        Button button = new Button("Add User");
        button.addStyleName(ValoTheme.TEXTFIELD_TINY);
        layout.addComponent(button);
        button.addClickListener(new Button.ClickListener()
        {
            @Override
            public void buttonClick(Button.ClickEvent event)
            {
                Person person = personComboBox.getValue();
                String email = emailField.getValue();

                if (person == null) { showError("Must select a person"); }
                else if (email.length() == 0) { showError("Email required"); }
                else if (userMgr.emailExists(email, orgMgr.getOrgId())) { showError("Email already exists"); }
                else
                {
                    userMgr.createUser(email, person.getId(), orgMgr.getOrgId());
                    set();
                }
            }
        });

        return layout;
    }

    private Component getUserGrid(Map<String, Person> peopleById)
    {
        Grid<User> grid = new Grid<>();
        grid.setSizeFull();

        Grid.Column<User, String> nameCol = addColumn(grid, LAST_FIRST_FIELD, User::getLastCommaFirst);
        addColumn(grid, EMAIL_FIELD, User::getEmail, User::setEmail);
        addColumn(grid, PASSWORD_FIELD, User::getPassword, User::setPassword);  // shows blank, clear text when typed in
        addBooleanColumn(grid, ENABLED_FIELD, User::getEnabled, User::setEnabled);
        addBooleanColumn(grid, ORG_ADMIN_FIELD, User::isOrgAdmin, User::setOrgAdmin);
        grid.addComponentColumn(this::buildDeleteButton);

        grid.sort(nameCol, SortDirection.ASCENDING);

        grid.getEditor().setEnabled(true);
        grid.getEditor().addSaveListener(event -> {
            // todo - check if email already taken
            User user = event.getBean();
            userMgr.save(user);
            msgDisplayer.displayMessage("User updates saved");
            set();
        });

        List<User> users = orgMgr.getUsers();
        users.forEach(u -> u.setPerson(peopleById.get(u.getPersonId())));

        Label countLabel = new Label();
        CountingDataProvider<User> dataProvider = new CountingDataProvider<User>(users, countLabel);
        grid.setDataProvider(dataProvider);

        HeaderRow filterHeader = grid.appendHeaderRow();
        setColumnFiltering(filterHeader, dataProvider);

        FooterRow footerRow = grid.appendFooterRow();
        footerRow.getCell(EMAIL_FIELD.getName()).setComponent(countLabel);

        return grid;
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

    private Button buildDeleteButton(User user)
    {
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

