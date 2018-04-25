package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.participant;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.selection.MultiSelectionEvent;
import com.vaadin.event.selection.MultiSelectionListener;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.FooterRow;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.viritin.fields.IntegerField;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.person.Person;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.BaseEventTagsAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.EventsAdminLayout;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.item.ItemMenuBar;
import xyz.cleangone.e2.web.vaadin.util.*;

import java.util.*;
import java.util.stream.Collectors;

import static xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant.LAST_COMMA_FIRST_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant.SELF_REGISTERED_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.person.Person.TAGS_FIELD;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createDeleteButton;

public class ParticipantsAdmin extends BaseEventTagsAdmin implements MultiSelectionListener<EventParticipant>
{
    private final ParticipantMenuBar participantMenuBar;

    private Map<String, OrgTag> allEventVisiblePersonTagsById;
    private List<OrgTag> eventPersonTags;
    private Map<String, OrgTag> eventPersonTagsById;
    private List<EventParticipant> selectedParticipants;

    public ParticipantsAdmin(EventsAdminLayout eventsAdminLayout, MessageDisplayer msgDisplayer)
    {
        super(eventsAdminLayout, OrgTag.TagType.PersonTag, msgDisplayer);
        participantMenuBar = new ParticipantMenuBar(this);

        setSizeFull();
        setMargin(false);
        setSpacing(true);
        setWidth("100%");
    }

    public void set()
    {
        super.set();

        List<OrgTag> allEventVisiblePersonTags = tagMgr.getEventVisibleTags(tagType, event);
        allEventVisiblePersonTagsById = tagMgr.getTagsById(allEventVisiblePersonTags);
        eventPersonTags = allEventVisiblePersonTags.stream()
            .filter(t -> event.getId().equals(t.getEventId()))
            .collect(Collectors.toList());
        eventPersonTagsById = tagMgr.getTagsById(eventPersonTags);

        List<OrgTag> orgTagsExposedToEvent = allEventVisiblePersonTags.stream()
            .filter(t -> !eventPersonTags.contains(t))
            .collect(Collectors.toList());

        removeAllComponents();

        Component grid = getParticipantGrid();

        participantMenuBar.setTagsForAddingParticipant(orgTagsExposedToEvent);
        participantMenuBar.setTagsToAdd(null);
        participantMenuBar.setTagsToRemove(null);

        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.setMargin(false);
        gridLayout.setSpacing(true);
        gridLayout.setStyleName("marginLeft");
        gridLayout.addComponents(grid);

        addComponents(participantMenuBar, gridLayout, new Label());
        setExpandRatio(gridLayout, 1.0f);
    }

    void addPeopleWithTag(OrgTag tag)
    {
        List<Person> people = orgMgr.getPeopleByTag(tag.getId());
        eventMgr.addEventParticipants(people);
        msgDisplayer.displayMessage("Participants added");
        set();
    }

    void addTagToSelectedPeople(OrgTag tag)
    {
        eventMgr.addTagId(tag.getId(), selectedParticipants);
        set();
    }

    void removeTagFromSelectedPeople(OrgTag tag)
    {
        eventMgr.removeTagId(tag.getId(), selectedParticipants);
        set();
    }

    private Grid<EventParticipant> getParticipantGrid()
    {
        Map<String, Person> peopleById = orgMgr.getPeopleByIdMap();

        Grid<EventParticipant> grid = new Grid<>();
        grid.setSizeFull();

        addColumn(grid, LAST_COMMA_FIRST_FIELD, EventParticipant::getLastCommaFirst, 1);
        addColumn(grid, TAGS_FIELD, EventParticipant::getTagsCsv, 2);

        if (event.getUserCanRegister()) { addBooleanColumn(grid, SELF_REGISTERED_FIELD, EventParticipant::getSelfRegistered, 1); }

        String countColLabel = eventMgr.getEvent().getIterationLabelPlural();
        if (countColLabel != null)
        {
            grid.addColumn(EventParticipant::getCount).setCaption(countColLabel).setExpandRatio(1)
                .setEditorComponent(new IntegerField(), EventParticipant::setCount);
        }

        grid.addComponentColumn(this::buildDeleteButton);

        grid.setColumnReorderingAllowed(true);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.asMultiSelect().addSelectionListener(this);

        List<EventParticipant> participants = eventMgr.getEventParticipants();
        participants.forEach(participant ->
            participant.setPerson(peopleById.get(participant.getPersonId()), allEventVisiblePersonTagsById));

        Label countLabel = new Label();
        CountingDataProvider<EventParticipant> dataProvider = new CountingDataProvider<>(participants, countLabel);
        grid.setDataProvider(dataProvider);

        grid.getEditor().setEnabled(true);
        grid.getEditor().setBuffered(true);
        grid.getEditor().addSaveListener(ev -> {
            EventParticipant participant = ev.getBean();
            eventMgr.save(participant);
            msgDisplayer.displayMessage("Participant saved");
            grid.setDataProvider(new ListDataProvider<>(participants));
        });

        HeaderRow filterHeader = grid.appendHeaderRow();
        setColumnFiltering(filterHeader, dataProvider);

        FooterRow footerRow = grid.appendFooterRow();
        footerRow.getCell(LAST_COMMA_FIRST_FIELD.getName()).setComponent(countLabel);

        return grid;
    }

    private Grid.Column<EventParticipant, String> addColumn(Grid<EventParticipant> grid,
        EntityField entityField, ValueProvider<EventParticipant, String> valueProvider, int expandRatio)
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

    private void setColumnFiltering(HeaderRow filterHeader, CountingDataProvider<EventParticipant> dataProvider)
    {
        MultiFieldFilter<EventParticipant> filter = new MultiFieldFilter<>(dataProvider);

        addFilterField(LAST_COMMA_FIRST_FIELD, EventParticipant::getLastCommaFirst, filter, filterHeader);
        addFilterField(TAGS_FIELD, EventParticipant::getTagsCsv, filter, filterHeader);
    }

    private void addFilterField(EntityField entityField,
        ValueProvider<EventParticipant, String> valueProvider, MultiFieldFilter<EventParticipant> filter, HeaderRow filterHeader)
    {
        filter.addField(entityField, valueProvider);
        VaadinUtils.addFilterField(entityField, filter, filterHeader);
    }

    private Button buildDeleteButton(EventParticipant participant)
    {
        Button button = createDeleteButton("Delete Event Participant");
        button.addClickListener(e -> {
            ConfirmDialog.show(getUI(), "Confirm Delete",
                "Delete Event Participant '" + participant.getPerson().getFirstLast() + "'?",
                "Delete", "Cancel", new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            eventMgr.delete(participant);
                            set();
                        }
                    }
                });
        });

        return button;
    }

    public void setOrgTagsDisclosureCaption() { tagsDisclosure.setDisclosureCaption(); }


    @Override
    public void selectionChange(MultiSelectionEvent<EventParticipant> event)
    {
        selectedParticipants = new ArrayList<>(event.getAllSelectedItems());
        if (selectedParticipants.isEmpty())
        {
            participantMenuBar.setTagsToAdd(null);
            participantMenuBar.setTagsToRemove(null);

            return;
        }

        participantMenuBar.setTagsToAdd(eventPersonTags);

        Set<String> selectedTagIds = new HashSet<>();
        for (EventParticipant participant : selectedParticipants)
        {
            // can only remove event tags
            List<String> participantEventTagIds = participant.getPerson().getTagIds().stream()
                .filter(id -> eventPersonTagsById.keySet().contains(id))
                .collect(Collectors.toList());

            selectedTagIds.addAll(participantEventTagIds);
        }

        List<OrgTag> selectedTags = selectedTagIds.stream()
            .map(tagId -> eventPersonTagsById.get(tagId))
            .collect(Collectors.toList());

        participantMenuBar.setTagsToRemove(selectedTags);
    }
}