package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.vaadin.icons.VaadinIcons;
import org.vaadin.addon.calendar.item.BasicItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.EventDate;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;

public class EventCalendarItem extends BasicItem
{
    private final EventDate eventDate;
    private final OrgEvent event;

    public EventCalendarItem(EventDate eventDate, OrgEvent event)
    {
        super(event.getName() + ": " + eventDate.getName(),
            eventDate.getDetails(), eventDate.getZoneDateTime());

        this.eventDate = eventDate;
        this.event = event;
    }

    public EventDate getEventDate() {
        return eventDate;
    }
    public OrgEvent getEvent() {
        return event;
    }

//    @Override
//    public String getStyleName() {
//        return "state-" + Meeting.State.empty;
////            meeting.getState().name().toLowerCase();
//    }


//    @Override
//    public boolean isAllDay() {
//        return false;
//    }

    public boolean isMoveable() { return false; }
    public boolean isResizeable() { return false; }


    @Override
    public String getDateCaptionFormat() {
        //return CalendarItem.RANGE_TIME;
        return VaadinIcons.CLOCK.getHtml()+" %s<br>" +
            VaadinIcons.ARROW_CIRCLE_RIGHT_O.getHtml()+" %s";
    }
}
