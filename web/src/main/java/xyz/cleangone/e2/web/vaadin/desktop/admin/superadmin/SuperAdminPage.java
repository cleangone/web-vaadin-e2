package xyz.cleangone.e2.web.vaadin.desktop.admin.superadmin;

import com.vaadin.data.ValueProvider;
import com.vaadin.server.Setter;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.e2.web.vaadin.desktop.admin.OrgAdminPage;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static xyz.cleangone.data.aws.dynamo.entity.base.BaseMixinEntity.*;
import static xyz.cleangone.data.aws.dynamo.entity.organization.Organization.*;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class SuperAdminPage extends BaseSuperAdminPage
{
    public static final String NAME = "Super";
    public static final String DISPLAY_NAME = "Super Admin";

    protected void set()
    {
        mainLayout.removeAllComponents();

        List<Organization> orgs = orgMgr.getAll();

        mainLayout.addComponent(getAddOrgLayout(orgs));
        if (!orgs.isEmpty()) { mainLayout.addComponent(getOrgsGrid(orgs)); }
    }

    private HorizontalLayout getAddOrgLayout(List<Organization> existingOrgs)
    {
        HorizontalLayout layout = horizontal(VaadinUtils.SIZE_UNDEFINED);

        TextField newOrgNameField = VaadinUtils.createGridTextField("New Organization Name");
        layout.addComponent(newOrgNameField);

        List<String> existingOrgNames = existingOrgs.stream()
            .map(Organization::getName)
            .collect(Collectors.toList());

        Button button = new Button("Add New Organization");
        button.addStyleName(ValoTheme.TEXTFIELD_TINY);
        layout.addComponent(button);
        VaadinUtils.addEnterKeyShortcut(button, newOrgNameField); // Enter key shortcut when field in focus
        button.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                String newOrgName = newOrgNameField.getValue();
                if (newOrgName != null && !existingOrgNames.contains(newOrgName))
                {
                    orgMgr.createOrg(newOrgName);
                    set();
                }
            }
        });

        return layout;
    }

    private Component getOrgsGrid(List<Organization> orgs)
    {
        Grid<Organization> grid = new Grid<>();

        Grid.Column nameCol = grid.addComponentColumn(this::buildNameLinkButton)
            .setCaption(NAME_FIELD.getDisplayName())
            .setComparator(Comparator.comparing(Organization::getName)::compare);
        addColumn(grid, TAG_FIELD, Organization::getTag, Organization::setTag);
        addBooleanColumn(grid, IS_DEFAULT_FIELD, Organization::getIsDefault, Organization::setIsDefault);

        grid.sort(nameCol, SortDirection.ASCENDING);

        grid.getEditor().setEnabled(true);
        grid.getEditor().addSaveListener(event -> {
            Organization org = event.getBean();
            orgMgr.save(org);
            set();
        });

        grid.setItems(orgs);

        return grid;
    }

    private Grid.Column<Organization, String> addColumn(Grid<Organization> grid, EntityField entityField,
        ValueProvider<Organization, String> valueProvider, Setter<Organization, String> setter)
    {
        return grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName()).setExpandRatio(1)
            .setEditorComponent(new TextField(), setter);
    }

    private Grid.Column<Organization, Boolean> addBooleanColumn(Grid<Organization> grid,
        EntityField entityField, ValueProvider<Organization, Boolean> valueProvider, Setter<Organization, Boolean> setter)
    {
        return grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName())
            .setEditorComponent(new CheckBox(), setter);
    }

    private Button buildNameLinkButton(Organization org)
    {
        return createLinkButton(org.getName(), e -> {
            orgMgr.setOrg(org);
            getUI().getNavigator().navigateTo(OrgAdminPage.NAME);
        });
    }

    public static String getName() { return NAME; }
    public static String getDisplayName() { return DISPLAY_NAME; }
}
