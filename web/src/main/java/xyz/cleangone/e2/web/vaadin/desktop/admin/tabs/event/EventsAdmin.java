package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.Setter;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.web.vaadin.ui.MessageDisplayer;
import xyz.cleangone.web.vaadin.util.VaadinUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent.*;
import static xyz.cleangone.web.vaadin.util.VaadinUtils.*;

public class EventsAdmin extends BaseEventAdmin
{
    private EventManager eventMgr;
    private UserManager userMgr;
    private OrgManager orgMgr;
    private String orgId;

    public EventsAdmin(EventsAdminLayout eventsAdminLayout, MessageDisplayer msgDisplayer)
    {
        super(eventsAdminLayout, msgDisplayer);
        setLayout(this, MARGIN_T, SPACING_TRUE, BACK_GREEN);
    }

    public void set(SessionManager sessionMgr)
    {
        eventMgr = sessionMgr.getEventManager();
        userMgr = sessionMgr.getUserManager();
        orgMgr = sessionMgr.getOrgManager();
        orgId = orgMgr.getOrgId();

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

        String eventCaption = orgMgr.getOrg().getEventCaption() == null ? "Event" : orgMgr.getOrg().getEventCaption();
        TextField addNameField = VaadinUtils.createGridTextField(eventCaption + " Name");
        Button button = createTextButton("Add " + eventCaption, e -> {
            OrgEvent event = eventMgr.createEvent(addNameField.getValue());
            set(); // display event created message
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

        User user = userMgr.getUser();
        if (user.isOrgAdmin(orgId))
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
            List<String> userAdminEventIds = user.getAdminPrivledgeEventIds(orgId);
            List<OrgEvent> allEvents = new ArrayList<>(events);

            events.clear();
            for (OrgEvent event : allEvents)
            {
                if (userAdminEventIds.contains(event.getId())) { events.add(event); }
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