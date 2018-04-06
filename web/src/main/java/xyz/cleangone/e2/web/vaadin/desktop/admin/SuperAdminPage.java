package xyz.cleangone.e2.web.vaadin.desktop.admin;

import static xyz.cleangone.data.aws.dynamo.entity.base.BaseMixinEntity.NAME_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.organization.Organization.TAG_FIELD;

import com.vaadin.data.ValueProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Setter;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.manager.VaadinSessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.ActionBar;
import xyz.cleangone.e2.web.vaadin.desktop.user.LoginPage;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;


import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;




public class SuperAdminPage extends VerticalLayout implements View
{
    public static final String NAME = "Super";
    public static final String DISPLAY_NAME = "SuperAdmin";

    private VerticalLayout mainLayout = new VerticalLayout();
    private ActionBar actionBar = new ActionBar();
    private SessionManager sessionMgr;

    public SuperAdminPage()
    {
        Panel mainPanel = new Panel("Super Admin");
        mainPanel.setSizeFull();

        mainPanel.setContent(mainLayout);
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        setMargin(true);
        setSpacing(true);
        setSizeFull();
        addComponents(actionBar, mainPanel);
        setExpandRatio(mainPanel, 1f);
    }

    @Override
    public void enter(ViewChangeEvent event)
    {
        sessionMgr = VaadinSessionManager.getExpectedSessionManager();
        if (!sessionMgr.hasSuperUser()) { getUI().getNavigator().navigateTo(LoginPage.NAME); }

        sessionMgr.resetOrg();
        actionBar.set(sessionMgr);
        set();
    }

    private void set()
    {
        mainLayout.removeAllComponents();

        List<Organization> orgs = sessionMgr.getOrgs();

        mainLayout.addComponent(getAddOrgLayout(orgs));

        if (!orgs.isEmpty())
        {
            mainLayout.addComponent(getOrgsGrid(orgs));
        }
    }

    private HorizontalLayout getAddOrgLayout(List<Organization> existingOrgs)
    {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeUndefined();

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
                    sessionMgr.createOrg(newOrgName);
                    set();
                }
            }
        });

        return layout;
    }

    private Component getOrgsGrid(List<Organization> orgs)
    {
        OrgManager orgMgr = sessionMgr.getOrgManager();

        Grid<Organization> grid = new Grid<>();

        Grid.Column nameCol = grid.addComponentColumn(this::buildNameLinkButton)
            .setCaption(NAME_FIELD.getDisplayName())
            .setComparator(Comparator.comparing(Organization::getName)::compare);
        addColumn(grid, TAG_FIELD, Organization::getTag, Organization::setTag);

        grid.sort(nameCol, SortDirection.ASCENDING);

        grid.getEditor().setEnabled(true);
        grid.getEditor().addSaveListener(event -> {
            Organization org = event.getBean();
            orgMgr.save(org);
        });

        grid.setItems(orgs);

        return grid;
    }

    private Grid.Column<Organization, String> addColumn(
        Grid<Organization> grid, EntityField entityField,
        ValueProvider<Organization, String> valueProvider, Setter<Organization, String> setter)
    {
        return grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName()).setExpandRatio(1)
            .setEditorComponent(new TextField(), setter);
    }

    private Button buildNameLinkButton(Organization org)
    {
        return VaadinUtils.createLinkButton(org.getName(), e -> {
            sessionMgr.setOrg(org);
            getUI().getNavigator().navigateTo(OrgAdminPage.NAME);
        });
    }

}
