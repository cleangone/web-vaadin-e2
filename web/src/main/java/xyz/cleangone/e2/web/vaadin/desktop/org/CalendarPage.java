package xyz.cleangone.e2.web.vaadin.desktop.org;

import com.vaadin.navigator.View;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.addon.calendar.Calendar;
import org.vaadin.addon.calendar.item.BasicItemProvider;
import org.vaadin.addon.calendar.ui.CalendarComponentEvents;
import xyz.cleangone.data.aws.dynamo.entity.organization.EventDate;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventCalendarItem;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CalendarPage extends BasePage implements View
{
    public static final String NAME = "Calendar";
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("EEE MM/dd/yyyy hh:mm a");

    public CalendarPage()
    {
        // todo - extend singleBannerPage
        super(BannerStyle.Single);
    }

    protected void set(SessionManager sessionManager)
    {
        super.set(sessionManager);

        sessionManager.resetEventManager();
        resetHeader();

        mainLayout.removeAllComponents();

        Component calendar = createCalendar(sessionManager.getEventManager());
        VerticalLayout calendarLayout = new VerticalLayout();
        calendarLayout.setMargin(true);
        calendarLayout.addComponent(calendar);

        mainLayout.addComponent(calendarLayout);
    }

    private Calendar<EventCalendarItem> createCalendar(EventManager eventMgr)
    {
        // show dates for all events
        List<EventDate> eventDates = eventMgr.getEventDates();
        Map<String, OrgEvent> eventsById = eventMgr.getEventsById();

        BasicItemProvider<EventCalendarItem> itemProvider = new BasicItemProvider<>();
        List<EventCalendarItem> calendarItems = eventDates.stream()
            .map(eventDate -> new EventCalendarItem(eventDate, eventsById.get(eventDate.getEventId())))
            .collect(Collectors.toList());
        itemProvider.setItems(calendarItems);

        Calendar<EventCalendarItem> calendar = new Calendar<>(itemProvider);
        calendar.addStyleName("meetings");
        calendar.setWidth("100%");
        calendar.setHeight("100%");

        calendar.setItemCaptionAsHtml(true);
        calendar.setContentMode(ContentMode.HTML);
        calendar.withMonth(ZonedDateTime.now().getMonth());

        calendar.setHandler(this::onCalendarClick);

        return calendar;
    }

    private void onCalendarClick(CalendarComponentEvents.ItemClickEvent event)
    {
        EventCalendarItem item = (EventCalendarItem)event.getCalendarItem();

        EventDate eventDate = item.getEventDate();
        OrgEvent orgEvent = item.getEvent();
        LocalDateTime ldt = eventDate.getLocalDateTime();
        String dateTimeString = ldt == null ? "" : ldt.format(DATE_TIME_FORMATTER);

        Notification.show(
            item.getCaption(), dateTimeString + eventDate.getDetails(), Notification.Type.HUMANIZED_MESSAGE);
    }
}
