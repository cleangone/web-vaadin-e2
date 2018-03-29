package xyz.cleangone.e2.web.vaadin.desktop.admin.event;

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
import xyz.cleangone.e2.web.vaadin.util.*;

import java.util.*;
import java.util.stream.Collectors;

import static xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant.LAST_COMMA_FIRST_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant.SELF_REGISTERED_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.person.Person.TAGS_FIELD;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createDeleteButton;


public class ParticipantsAdmin extends BaseEventTagsAdmin implements MultiSelectionListener<EventParticipant>
{
    private List<OrgTag> eventPersonTags;
    private Map<String, OrgTag> eventPersonTagsById;

    private ComboBox<OrgTag> addTagComboBox = new ComboBox<>();
    private ComboBox<OrgTag> removeTagComboBox = new ComboBox<>();
    private List<EventParticipant> selectedParticipants;


    public ParticipantsAdmin(EventsAdminPage eventsAdmin, MessageDisplayer msgDisplayer)
    {
        super(eventsAdmin, OrgTag.TagType.PersonTag, msgDisplayer);

        setSizeFull();
        setMargin(new MarginInfo(true, false, false, false)); // T/R/B/L
        setSpacing(true);
        setWidth("100%");
    }

    public void set()
    {
        super.set();

        eventPersonTags = tagMgr.getEventVisibleTags(tagType, event);
        eventPersonTagsById = tagMgr.getTagsById(eventPersonTags);

        removeAllComponents();

        Component grid = getParticipantGrid();
        addComponents(getAddParticipantLayout(), grid, new Label());
        setExpandRatio(grid, 1.0f);

        addTagComboBox.setItems(eventPersonTags);
        addTagComboBox.setValue(null);  // todo - there is a bug with setValue that is being fixed
        removeTagComboBox.setValue(null);
    }

    private Component getAddParticipantLayout()
    {
        GridLayout barLayout = new GridLayout(2, 1);
        barLayout.setSizeUndefined();
        barLayout.setWidth("100%");

        HorizontalLayout leftLayout = new HorizontalLayout();
        barLayout.addComponent(leftLayout);

        HorizontalLayout rightLayout = new HorizontalLayout();
        barLayout.addComponent(rightLayout);
        barLayout.setComponentAlignment(rightLayout, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));

        ComboBox<OrgTag> tagComboBox = new ComboBox<>();
        tagComboBox.addStyleName(ValoTheme.TEXTFIELD_TINY);
        tagComboBox.setPlaceholder("Tag");
        tagComboBox.setItems(tagMgr.getOrgTags(tagType));
        tagComboBox.setItemCaptionGenerator(OrgTag::getName);

        Button button = new Button("Add People with Tag");
        button.addStyleName(ValoTheme.TEXTFIELD_TINY);
        button.addClickListener(new Button.ClickListener()
        {
            @Override
            public void buttonClick(Button.ClickEvent event)
            {
                OrgTag tag = tagComboBox.getValue();
                if (tag == null) { return; }

                List<Person> people = orgMgr.getPeopleByTag(tag.getId());
                eventMgr.addEventParticipants(people);
                msgDisplayer.displayMessage("Participants added");
                set();
            }
        });

        leftLayout.addComponents(tagComboBox, button);

        addTagComboBox.addStyleName(ValoTheme.TEXTFIELD_TINY);
        addTagComboBox.setPlaceholder("Add Tag");
        addTagComboBox.setItemCaptionGenerator(OrgTag::getName);
        addTagComboBox.setEnabled(false);
        addTagComboBox.addValueChangeListener(event -> addTagToSelectedPeople());

        removeTagComboBox.addStyleName(ValoTheme.TEXTFIELD_TINY);
        removeTagComboBox.setPlaceholder("Remove Tag");
        removeTagComboBox.setItemCaptionGenerator(OrgTag::getName);
        removeTagComboBox.setEnabled(false);
        removeTagComboBox.addValueChangeListener(event -> removeTagFromSelectedPeople());

        rightLayout.addComponents(new Label("Tags"), addTagComboBox, removeTagComboBox);
        rightLayout.setComponentAlignment(addTagComboBox, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));
        rightLayout.setComponentAlignment(removeTagComboBox, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));

        return barLayout;
    }

    private void addTagToSelectedPeople()
    {
        OrgTag tag = addTagComboBox.getValue();
        if (tag == null) { return; }

        eventMgr.addTagId(tag.getId(), selectedParticipants);
        set();
    }

    private void removeTagFromSelectedPeople()
    {
        OrgTag tag = removeTagComboBox.getValue();
        if (tag == null) { return; }

        eventMgr.removeTagId(tag.getId(), selectedParticipants);
        set(); // at least one participant will have been updated // TODO - overkill
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
            participant.setPerson(peopleById.get(participant.getPersonId()), eventPersonTagsById));

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
            addTagComboBox.setEnabled(false);

            removeTagComboBox.setItems(Collections.emptyList());
            removeTagComboBox.setEnabled(false);
            return;
        }

        addTagComboBox.setEnabled(true);

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

        removeTagComboBox.setItems(selectedTags);
        removeTagComboBox.setEnabled(!selectedTags.isEmpty());
    }
}