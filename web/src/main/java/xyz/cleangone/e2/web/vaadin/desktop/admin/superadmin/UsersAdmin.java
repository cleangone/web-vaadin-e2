package xyz.cleangone.e2.web.vaadin.desktop.admin.superadmin;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.Setter;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.FooterRow;
import com.vaadin.ui.components.grid.HeaderRow;
import org.vaadin.dialogs.ConfirmDialog;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.BaseAdmin;
import xyz.cleangone.web.vaadin.util.CountingDataProvider;
import xyz.cleangone.web.vaadin.ui.MessageDisplayer;
import xyz.cleangone.web.vaadin.util.VaadinUtils;

import java.util.List;
import java.util.stream.Collectors;

import static xyz.cleangone.data.aws.dynamo.entity.person.User.*;
import static xyz.cleangone.web.vaadin.util.VaadinUtils.*;

public class UsersAdmin extends BaseAdmin
{
    private UserManager userMgr;

    public UsersAdmin(MessageDisplayer msgDisplayer)
    {
        super(msgDisplayer);
        setLayout(this, MARGIN_TRB, SPACING_TRUE, SIZE_FULL);
    }

    public void set(SessionManager sessionMgr)
    {
        userMgr = sessionMgr.getUserManager();
        set();
    }

    public void set()
    {
        removeAllComponents();

        Component grid = getUserGrid();
        addComponents(grid);
        setExpandRatio(grid, 1.0f);
    }

    private Component getUserGrid()
    {
        Grid<User> grid = new Grid<>();
        grid.setSizeFull();

        Grid.Column<User, String> nameCol = addColumn(grid, LAST_FIRST_FIELD, User::getLastCommaFirst);
        addColumn(grid, EMAIL_FIELD, User::getEmail);
        addColumn(grid, PASSWORD_FIELD, User::getPassword, User::setPassword);  // shows blank, clear text when typed in
        addBooleanColumn(grid, ENABLED_FIELD, User::getEnabled, User::setEnabled);
        grid.addComponentColumn(this::buildDeleteButton);

        grid.sort(nameCol, SortDirection.ASCENDING);

        grid.getEditor().setEnabled(true);
        grid.getEditor().addSaveListener(event -> {
            User user = event.getBean();
            userMgr.save(user);
            msgDisplayer.displayMessage("User updates saved");
            set();
        });

        List<User> users = userMgr.getUsers().stream()
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

