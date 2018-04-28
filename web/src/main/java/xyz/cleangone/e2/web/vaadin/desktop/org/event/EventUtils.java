package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.kim.countdownclock.CountdownClock;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant;
import xyz.cleangone.data.manager.ActionManager;
import xyz.cleangone.data.manager.event.BidStatus;
import xyz.cleangone.e2.web.manager.OutbidEmailSender;
import xyz.cleangone.e2.web.vaadin.desktop.broadcast.Broadcaster;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class EventUtils
{
    public static long ONE_HOUR = 1000 * 60 * 60;
    public static long FIVE_DAYS = ONE_HOUR * 24 * 5;

    public static ComboBox<EventParticipant> createParticipantComboBox(List<EventParticipant> participants)
    {
        ComboBox<EventParticipant> comboBox = new ComboBox<>("Participant");
        comboBox.addStyleName(ValoTheme.TEXTFIELD_TINY);
        comboBox.setItems(participants);
        comboBox.setItemCaptionGenerator(EventParticipant::getLastCommaFirst);

        return comboBox;
    }

    public static Label getSoldLabel()
    {
        return getCautionLabel("Sold");
    }

    public static Label getCautionLabel(String text)
    {
        return getLabel(text, "caution");
    }

    public static Label getGoodNewsLabel(String text)
    {
        return getLabel(text, "blueBold");
    }

    public static Label getLabel(String value, String styleName)
    {
        Label label = new Label(value);
        label.setStyleName(styleName);
        return label;
    }

    public static boolean showCountdownClock(Date endDate)
    {
        Date now = new Date();
        return (endDate != null) &&
            (endDate.getTime() + 500 > now.getTime()) &&  // add half second for race
            (endDate.getTime() - ONE_HOUR < now.getTime());
    }

    public static CountdownClock getCountdownClock(Date endDate)
    {
        CountdownClock clock = new CountdownClock();
        clock.setDate(endDate);
        clock.setFormat("%m min %s sec");
        return clock;
    }
}
