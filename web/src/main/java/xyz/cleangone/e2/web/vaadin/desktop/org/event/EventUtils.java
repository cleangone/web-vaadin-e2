package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant;

import java.util.List;

public class EventUtils
{
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

    public static Label getWinningLabel(String text)
    {
        return getLabel(text, "blueBold");
    }

    public static Label getLabel(String value, String styleName)
    {
        Label label = new Label(value);
        label.setStyleName(styleName);
        return label;
    }

}
