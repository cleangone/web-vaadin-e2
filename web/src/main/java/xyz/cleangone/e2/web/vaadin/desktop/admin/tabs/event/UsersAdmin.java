package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event;

import com.vaadin.data.ValueProvider;
import com.vaadin.event.selection.MultiSelectionEvent;
import com.vaadin.event.selection.MultiSelectionListener;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;

import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.util.CountingDataProvider;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.MultiFieldFilter;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant.*;
import static xyz.cleangone.data.aws.dynamo.entity.person.User.*;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class UsersAdmin extends BaseEventAdmin implements MultiSelectionListener<User>
{
    private List<OrgTag> eventUserRoles;
    private Map<String, OrgTag> eventUserRolesById;

    private EventManager eventMgr;
    private OrgManager orgMgr;
    private TagManager tagMgr;
    private UserManager userMgr;
    protected OrgEvent event;

    private ComboBox<OrgTag> addTagComboBox = new ComboBox<>();
    private ComboBox<OrgTag> removeTagComboBox = new ComboBox<>();
    private List<User> selectedUsers;


    public UsersAdmin(EventsAdminLayout eventsAdminLayout, MessageDisplayer msgDisplayer)
    {
        super(eventsAdminLayout, msgDisplayer);
        setLayout(this, MARGIN_TR, SPACING_TRUE, SIZE_FULL, WIDTH_100_PCT);
    }

    public void set(SessionManager sessionMgr)
    {
        eventMgr = sessionMgr.getEventManager();
        orgMgr = sessionMgr.getOrgManager();
        tagMgr = orgMgr.getTagManager();
        userMgr = sessionMgr.getUserManager();
    }

    public void set()
    {
        event = requireNonNull(eventMgr.getEvent());

        eventUserRoles = tagMgr.getEventTags(OrgTag.TagType.UserRole, event.getId());
        eventUserRolesById = tagMgr.getTagsById(eventUserRoles);

        removeAllComponents();

        Component grid = getUsersGrid();
        addComponents(getUpdateTagsLayout(), grid, new Label());
        setExpandRatio(grid, 1.0f);

        addTagComboBox.setItems(eventUserRoles);
        addTagComboBox.setValue(null);  // todo - there is a bug with setValue that is being fixed
        removeTagComboBox.setValue(null);
    }

    private Component getUpdateTagsLayout()
    {
        HorizontalLayout leftLayout = new HorizontalLayout();
        HorizontalLayout rightLayout = new HorizontalLayout();

        HorizontalLayout barLayout = new HorizontalLayout();
        barLayout.setSizeUndefined();
        barLayout.setWidth("100%");
        barLayout.addComponents(leftLayout, rightLayout);
        barLayout.setExpandRatio(leftLayout, 1.0f);
        barLayout.setComponentAlignment(rightLayout, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));

        addTagComboBox.addStyleName(ValoTheme.TEXTFIELD_TINY);
        addTagComboBox.setPlaceholder("Add User Role");
        addTagComboBox.setItemCaptionGenerator(OrgTag::getName);
        addTagComboBox.setEnabled(false);
        addTagComboBox.addValueChangeListener(event -> addTagToSelectedPeople());

        removeTagComboBox.addStyleName(ValoTheme.TEXTFIELD_TINY);
        removeTagComboBox.setPlaceholder("Remove User Role");
        removeTagComboBox.setItemCaptionGenerator(OrgTag::getName);
        removeTagComboBox.setEnabled(false);
        removeTagComboBox.addValueChangeListener(event -> removeTagFromSelectedPeople());

        rightLayout.addComponents(new Label("User Roles"), addTagComboBox, removeTagComboBox);
        rightLayout.setComponentAlignment(addTagComboBox, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));
        rightLayout.setComponentAlignment(removeTagComboBox, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));

        return barLayout;
    }

    private void addTagToSelectedPeople()
    {
        OrgTag tag = addTagComboBox.getValue();
        if (tag == null) { return; }

        userMgr.addTagId(tag.getId(), selectedUsers);
        set();
    }

    private void removeTagFromSelectedPeople()
    {
        OrgTag tag = removeTagComboBox.getValue();
        if (tag == null) { return; }

        userMgr.removeTagId(tag.getId(), selectedUsers);
        set(); // at least one participant will have been updated // TODO - overkill
    }

    private Grid<User> getUsersGrid()
    {
        // get enabled users that are not already an admin
        List<User> users = orgMgr.getUsers().stream()
            .filter(u -> u.getEnabled())
            .filter(u -> !u.isOrgAdmin(orgMgr.getOrgId())) // todo - why do this?
            .collect(Collectors.toList());

        for (User user : users)
        {
            user.setTagsCsv(eventUserRolesById);
        }

        Grid<User> grid = new Grid<>();
        grid.setSizeFull();

        addColumn(grid, LAST_COMMA_FIRST_FIELD, User::getLastCommaFirst, 1);
        addColumn(grid, TAGS_FIELD, User::getTagsCsv, 2);

        grid.addColumn(this::isAdmin)
            .setId(ADMIN_FIELD.getName()).setCaption(ADMIN_FIELD.getDisplayName())
            .setEditorComponent(new CheckBox(), this::setAdmin);

        grid.setColumnReorderingAllowed(true);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.asMultiSelect().addSelectionListener(this);

        grid.getEditor().setEnabled(true);
        grid.getEditor().addSaveListener(event -> {
            User user = event.getBean();
            userMgr.save(user);
            msgDisplayer.displayMessage("User updates saved");
            set();
        });

        Label countLabel = new Label();
        CountingDataProvider<User> dataProvider = new CountingDataProvider<>(users, countLabel);
        grid.setDataProvider(dataProvider);

        HeaderRow filterHeader = grid.appendHeaderRow();
        setColumnFiltering(filterHeader, dataProvider);

        return grid;
    }

    private boolean isAdmin(User user)
    {
        return user.isEventAdmin(event.getOrgId(), event.getId());
    }
    private void setAdmin(User user, boolean isAdmin)
    {
        if (isAdmin) { user.addAdminPrivledge(event); }
        else { user.removeAdminPrivledge(event); }
    }

    private Grid.Column<User, String> addColumn(Grid<User> grid,
        EntityField entityField, ValueProvider<User, String> valueProvider, int expandRatio)
    {
        return grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName()).setExpandRatio(expandRatio);
    }

    private Grid.Column<EventParticipant, Boolean> addBooleanColumn(Grid<EventParticipant> grid,
        EntityField entityField, ValueProvider<EventParticipant, Boolean> valueProvider, int expandRatio)
    {
        return grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName()).setExpandRatio(expandRatio);
    }

    private void setColumnFiltering(HeaderRow filterHeader, CountingDataProvider<User> dataProvider)
    {
        MultiFieldFilter<User> filter = new MultiFieldFilter<>(dataProvider);

        addFilterField(LAST_COMMA_FIRST_FIELD, User::getLastCommaFirst, filter, filterHeader);
        addFilterField(TAGS_FIELD, User::getTagsCsv, filter, filterHeader);
    }

    private void addFilterField(EntityField entityField,
        ValueProvider<User, String> valueProvider, MultiFieldFilter<User> filter, HeaderRow filterHeader)
    {
        filter.addField(entityField, valueProvider);
        VaadinUtils.addFilterField(entityField, filter, filterHeader);
    }

    @Override
    public void selectionChange(MultiSelectionEvent<User> event)
    {
        selectedUsers = new ArrayList<>(event.getAllSelectedItems());
        if (selectedUsers.isEmpty())
        {
            addTagComboBox.setEnabled(false);

            removeTagComboBox.setItems(Collections.emptyList());
            removeTagComboBox.setEnabled(false);
            return;
        }

        addTagComboBox.setEnabled(true);

        Set<String> selectedTagIds = new HashSet<>();
        for (User user : selectedUsers)
        {
            // can only remove event tags
            List<String> allUserTagIds = user.getTagIds().stream()
                .filter(id -> eventUserRolesById.keySet().contains(id))
                .collect(Collectors.toList());

            selectedTagIds.addAll(allUserTagIds);
        }

        List<OrgTag> selectedTags = selectedTagIds.stream()
            .map(tagId -> eventUserRolesById.get(tagId))
            .collect(Collectors.toList());

        removeTagComboBox.setItems(selectedTags);
        removeTagComboBox.setEnabled(!selectedTags.isEmpty());
    }
}