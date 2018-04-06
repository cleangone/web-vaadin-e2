package xyz.cleangone.e2.web.vaadin.desktop.admin.event;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.Setter;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent.*;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createTextButton;

public class EventsAdmin extends BaseEventAdmin
{
    private EventManager eventMgr;
    private UserManager userMgr;
    private OrgManager orgMgr;
    private TagManager tagMgr;


    public EventsAdmin(EventsAdminLayout eventsAdminLayout, MessageDisplayer msgDisplayer)
    {
        super(eventsAdminLayout, msgDisplayer);

        setMargin(new MarginInfo(true, false, false, false)); // T/R/B/L
        setSpacing(true);
    }

    public void set(SessionManager sessionMgr)
    {
        eventMgr = sessionMgr.getEventManager();
        userMgr = sessionMgr.getUserManager();
        orgMgr = sessionMgr.getOrgManager();
        tagMgr = orgMgr.getTagManager();

        set();
    }

    // todo - reset that does not rebuild addEventlayout
    public void set()
    {
        removeAllComponents();
        Component grid = getEventGrid();
        addComponents(getAddEventLayout(), grid, new Label());
        setExpandRatio(grid, 1.0f);
    }

    private Component getAddEventLayout()
    {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeUndefined();

        TextField addNameField = VaadinUtils.createGridTextField("Event Name");
        Button button = createTextButton("Add Event", e -> {
            OrgEvent event = eventMgr.createEvent(addNameField.getValue());
            tagMgr.createTag(OrgTag.ADMIN_ROLE_NAME, OrgTag.TagType.UserRole, event);

            // display event created message
            set();
        });

        layout.addComponents(addNameField, button);
        return layout;
    }

    private Grid<OrgEvent> getEventGrid()
    {
        Grid<OrgEvent> grid = new Grid<>();
        grid.setSizeFull();
        grid.setWidth("75%");

        grid.addComponentColumn(this::buildNameLinkButton)
            .setCaption(NAME_FIELD.getDisplayName())
            .setComparator(Comparator.comparing(OrgEvent::getName)::compare);

        addColumn(grid, DISPLAY_COL_FIELD, OrgEvent::getDisplayColString, OrgEvent::setDisplayColString);
        Grid.Column<OrgEvent, String> dispOrderCol = addColumn(grid, DISPLAY_ORDER_FIELD, OrgEvent::getDisplayOrder, OrgEvent::setDisplayOrder);
        addBooleanColumn(grid, ENABLED_FIELD, OrgEvent::getEnabled, OrgEvent::setEnabled);
        addColumn(grid, TAG_FIELD, OrgEvent::getTag, OrgEvent::setTag);

        grid.sort(dispOrderCol, SortDirection.ASCENDING);

        List<OrgEvent> events = new ArrayList<>(eventMgr.getEvents());
        if (userMgr.userIsAdmin(orgMgr.getOrg()))
        {
            // user is an admin and can edit grid entries
            grid.getEditor().setEnabled(true);
            grid.getEditor().addSaveListener(event ->
            {
                OrgEvent orgEvent = event.getBean();
                eventMgr.save(orgEvent);
                set();
            });
        }
        else
        {
            // user is an event admin of at least one event - determine which events to display
            Map<String, OrgTag> eventAdminRoleTagsById = tagMgr.getEventAdminRoleTagsById();
            Map<String, OrgEvent> eventsById = events.stream()
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

            events.clear();
            for (String tagId : userMgr.getUser().getTagIds())
            {
                if (eventAdminRoleTagsById.keySet().contains(tagId))
                {
                    OrgTag adminRoleTag = eventAdminRoleTagsById.get(tagId);
                    OrgEvent event = eventsById.get(adminRoleTag.getEventId());
                    if (event != null) { events.add(event); }
                }
            }
        }

        grid.setDataProvider(new ListDataProvider<>(events));
        return grid;
    }

    private Grid.Column<OrgEvent, String> addColumn(
        Grid<OrgEvent> grid, EntityField entityField,
        ValueProvider<OrgEvent, String> valueProvider, Setter<OrgEvent, String> setter)
    {
        return grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName()).setExpandRatio(1)
            .setEditorComponent(new TextField(), setter);
    }

    private void addBooleanColumn(
        Grid<OrgEvent> grid, EntityField entityField, ValueProvider<OrgEvent, Boolean> valueProvider, Setter<OrgEvent, Boolean> setter)
    {
        grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName())
            .setEditorComponent(new CheckBox(), setter);
    }

    private Button buildNameLinkButton(OrgEvent event)
    {
        return VaadinUtils.createLinkButton(event.getName(), e -> {
            eventMgr.setEvent(event);
            eventsAdminLayout.setAdminPage(EventAdminPageType.GENERAL);
        });
    }
}