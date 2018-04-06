package xyz.cleangone.e2.web.vaadin.desktop.admin.event;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.Setter;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.LocalDateTimeRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.EventDate;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.time.LocalDateTime;
import java.util.List;

import static xyz.cleangone.data.aws.dynamo.entity.organization.EventDate.DATE_NAME_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.organization.EventDate.DETAILS_FIELD;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createDeleteButton;


public class DatesAdmin extends BaseEventAdmin
{
    private EventManager eventMgr;

    public DatesAdmin(EventsAdminLayout eventsAdmin, MessageDisplayer msgDisplayer)
    {
        super(eventsAdmin, msgDisplayer);

        setSizeFull();
        setMargin(new MarginInfo(true, false, false, false)); // T/R/B/L
        setSpacing(true);
        setWidth("100%");
    }

    public void set(SessionManager sessionMgr)
    {
        eventMgr = sessionMgr.getEventManager();
    }

    public void set()
    {
        removeAllComponents();

        Component grid = getDateGrid();
        addComponents(getAddDateLayout(), grid, new Label());
        setExpandRatio(grid, 1.0f);
    }

    private Component getAddDateLayout()
    {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeUndefined();

        TextField addNameField = VaadinUtils.createGridTextField("Date Title");

        Button button = new Button("Add Date");
        button.addStyleName(ValoTheme.TEXTFIELD_TINY);
        button.addClickListener(new Button.ClickListener()
        {
            @Override
            public void buttonClick(Button.ClickEvent event)
            {
                eventMgr.createEventDate(addNameField.getValue());
                msgDisplayer.displayMessage("Event Date added");
                set();
            }
        });

        layout.addComponents(addNameField, button);

        return layout;
    }

    private Grid<EventDate> getDateGrid()
    {
        Grid<EventDate> grid = new Grid<>();
        grid.setSizeFull();
        grid.setWidth("70%");

        addColumn(grid, DATE_NAME_FIELD, EventDate::getName, EventDate::setName);
        addColumn(grid, DETAILS_FIELD, EventDate::getDetails, EventDate::setDetails);

        Grid.Column<EventDate, LocalDateTime> dateTimeColumn =
            grid.addColumn(EventDate::getLocalDateTime, new LocalDateTimeRenderer("EEE MM/dd/yyyy hh:mm a"))
                .setCaption("Date/Time")
                .setEditorComponent(new DateTimeField(), EventDate::setLocalDateTime);
        grid.addComponentColumn(this::buildDeleteButton);

        grid.sort(dateTimeColumn, SortDirection.ASCENDING);

        grid.getEditor().setEnabled(true);
        grid.getEditor().addSaveListener(event -> {
            EventDate eventDate = event.getBean();
            eventMgr.save(eventDate);
            msgDisplayer.displayMessage("Event Date saved");
        });

        List<EventDate> dates = eventMgr.getEventDatesByEvent();
        grid.setDataProvider(new ListDataProvider<EventDate>(dates));

        return grid;
    }

    private Component buildDeleteButton(EventDate date)
    {
        Button button = createDeleteButton("Delete Event Date");
        button.addClickListener(e -> {
            ConfirmDialog.show(getUI(), "Confirm Date Delete", "Delete date '" + date.getName() + "'?",
                "Delete", "Cancel", new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            eventMgr.delete(date);
                            set();
                        }
                    }
                });
        });

        return button;
    }

    private Grid.Column<EventDate, String> addColumn(
        Grid<EventDate> grid, EntityField entityField,
        ValueProvider<EventDate, String> valueProvider, Setter<EventDate, String> setter)
    {
        return grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName())
            .setEditorComponent(new TextField(), setter);
    }
}