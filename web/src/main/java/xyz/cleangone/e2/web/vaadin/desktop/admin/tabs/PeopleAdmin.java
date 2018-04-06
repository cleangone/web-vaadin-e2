package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs;


import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createDeleteButton;
import static xyz.cleangone.data.aws.dynamo.entity.person.Person.*;

import com.vaadin.data.ValueProvider;
import com.vaadin.event.selection.MultiSelectionEvent;
import com.vaadin.event.selection.MultiSelectionListener;
import com.vaadin.server.Setter;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.shared.ui.grid.DropMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.FooterRow;
import com.vaadin.ui.components.grid.GridDropTarget;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.person.Person;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.e2.web.manager.EntityChangeManager;
import xyz.cleangone.e2.web.vaadin.util.CountingDataProvider;
import xyz.cleangone.e2.web.vaadin.util.MultiFieldFilter;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PeopleAdmin extends VerticalLayout implements MultiSelectionListener<Person>
{
    private final MessageDisplayer msgDisplayer;

    private OrgManager orgMgr;
    private EntityChangeManager changeManager = new EntityChangeManager();

    // add/remove tag from selected people
    private Map<String, OrgTag> orgTagsById;
    private ComboBox<OrgTag> addTagComboBox = new ComboBox<>();
    private ComboBox<OrgTag> removeTagComboBox = new ComboBox<>();;

    // grid of people
    private List<Person> selectedPeople;
    private CountingDataProvider<Person> dataProvider;
    private Grid<Person> personGrid;

    public PeopleAdmin(MessageDisplayer msgDisplayer)
    {
        this.msgDisplayer = msgDisplayer;

        Label filteredItemCountLabel = new Label();
        dataProvider = new CountingDataProvider<>(new ArrayList<>(), filteredItemCountLabel);
        personGrid = getPersonGrid(filteredItemCountLabel);

        setSizeFull();
        setMargin(new MarginInfo(true, false, true, false)); // T/R/B/L
        setSpacing(true);

        addComponents(getTopBarLayout(), personGrid);
        setExpandRatio(personGrid, 1.0f);
    }

    public void set(OrgManager orgMgr)
    {
        this.orgMgr = orgMgr;
        set();
    }

    public void set()
    {
        Organization org = orgMgr.getOrg();
        if (changeManager.unchanged(org) &&
            changeManager.unchanged(org, EntityType.PersonTag, EntityType.Person))
        {
            return;
        }

        changeManager.reset(org);

        TagManager tagMgr = orgMgr.getTagManager();
        List<OrgTag> orgTags = tagMgr.getPersonTags();
        orgTagsById = tagMgr.getTagsById(orgTags);

        List<Person> people = orgMgr.getPeople();
        if (!people.isEmpty() && people.get(0).getTagsCsv() == null)
        {
            // set tagsCsv
            people.forEach(person -> person.setTagsCsv(orgTagsById));
        }

        dataProvider.resetItems(people);
        personGrid.setDataProvider(dataProvider); //  todo - workaround for grid/dataprovider bug

        addTagComboBox.setItems(orgTags);
        addTagComboBox.setValue(null);  // todo - there is a bug with setValue that is being fixed
        removeTagComboBox.setValue(null);
    }

    private Grid<Person> getPersonGrid(Label filteredItemCountLabel)
    {
        Grid<Person> grid = new Grid<>();
        grid.setWidth("100%");
        grid.setHeight("100%");
//        grid.setHeightMode(HeightMode.CSS);

        Grid.Column<Person, Date> createdDateCol = grid.addColumn(Person::getCreatedDate).setCaption("Date Added")
            .setId(CREATED_DATE_FIELD.getName())
            .setRenderer(new DateRenderer(DateFormat.getDateInstance(DateFormat.MEDIUM)));
        addColumn(grid, FIRST_NAME_FIELD, Person::getFirstName, Person::setFirstName, 1);
        Grid.Column<Person, String> lastNameCol =
            addColumn(grid, LAST_NAME_FIELD, Person::getLastName, Person::setLastName, 1);

        addColumn(grid, TAGS_FIELD, Person::getTagsCsv, 2);
        Grid.Column actionCol = grid.addComponentColumn(this::buildDeleteButton);

        grid.sort(lastNameCol, SortDirection.ASCENDING);
        grid.setColumnReorderingAllowed(true);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.asMultiSelect().addSelectionListener(this);

        grid.getColumns().stream()
            .filter(column -> column != actionCol)
            .forEach(column -> column.setHidable(true));
        createdDateCol.setHidden(true);


        GridDropTarget<Person> dropTarget = new GridDropTarget<>(grid, DropMode.BETWEEN);
        dropTarget.setDropEffect(DropEffect.MOVE);

        dropTarget.addGridDropListener(event -> {
            // Accepting dragged items from another Grid in the same UI
            int i=1;
        });


        grid.getEditor().setEnabled(true);
        grid.getEditor().addSaveListener(event -> savePerson(event.getBean()));

        grid.setDataProvider(dataProvider);

        HeaderRow filterHeader = grid.appendHeaderRow();
        setColumnFiltering(filterHeader, dataProvider);

        FooterRow footerRow = grid.appendFooterRow();
        footerRow.getCell(FIRST_NAME_FIELD.getName()).setComponent(filteredItemCountLabel);

        return grid;
    }

    private void savePerson(Person person)
    {
        orgMgr.setPerson(person);
        set();
    }

    private Grid.Column<Person, String> addColumn(
        Grid<Person> grid, EntityField entityField,
        ValueProvider<Person, String> valueProvider, Setter<Person, String> setter, int expandRatio)
    {
        return addColumn(grid, entityField, valueProvider, expandRatio).setEditorComponent(new TextField(), setter);
    }

    private Grid.Column<Person, String> addColumn(
        Grid<Person> grid, EntityField entityField, ValueProvider<Person, String> valueProvider, int expandRatio)
    {
        return grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName()).setExpandRatio(expandRatio);
    }

    private Button buildDeleteButton(Person person)
    {
        return createDeleteButton("Delete '" + person.getFirstLast()  + "'", getUI(), new ConfirmDialog.Listener() {
            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    orgMgr.deletePerson(person);
                    set();
                }
            }
        });
    }

    private void setColumnFiltering(HeaderRow filterHeader, CountingDataProvider<Person> dataProvider)
    {
        MultiFieldFilter<Person> filter = new MultiFieldFilter<>(dataProvider);

        addFilterField(FIRST_NAME_FIELD, Person::getFirstName, filter, filterHeader);
        addFilterField(LAST_NAME_FIELD, Person::getLastName, filter, filterHeader);
        addFilterField(TAGS_FIELD, Person::getTagsCsv, filter, filterHeader);
    }

    private void addFilterField(
        EntityField entityField, ValueProvider<Person, String> valueProvider, MultiFieldFilter<Person> filter, HeaderRow filterHeader)
    {
        filter.addField(entityField, valueProvider);
        VaadinUtils.addFilterField(entityField, filter, filterHeader);
    }

    private GridLayout getTopBarLayout()
    {
        GridLayout barLayout = new GridLayout(3, 1);
        barLayout.setSizeUndefined();
        barLayout.setWidth("100%");

        HorizontalLayout leftLayout = new HorizontalLayout();
        barLayout.addComponent(leftLayout);

        barLayout.addComponent(new HorizontalLayout());

        HorizontalLayout rightLayout = new HorizontalLayout();
        barLayout.addComponent(rightLayout);
        barLayout.setComponentAlignment(rightLayout, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));

        TextField addFirstNameField = VaadinUtils.createGridTextField("First Name");
        TextField addLastNameField = VaadinUtils.createGridTextField("Last Name");

        Button addPersonButton = new Button("Add Person");
        addPersonButton.addStyleName(ValoTheme.TEXTFIELD_TINY);
        addPersonButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event)
            { createPerson(addFirstNameField.getValue(), addLastNameField.getValue()); }
        });

        PopupView importPopup = new PopupView(null, new ImportPeoplePanel());
        Button importButton = VaadinUtils.createLinkButton("Bulk Import", ev -> importPopup.setPopupVisible(true));

        leftLayout.addComponents(addFirstNameField, addLastNameField, addPersonButton, importButton, importPopup);

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

        rightLayout.addComponent(new Label("Tags"));
        rightLayout.addComponent(addTagComboBox);
        rightLayout.setComponentAlignment(addTagComboBox, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));
        rightLayout.addComponent(removeTagComboBox);
        rightLayout.setComponentAlignment(removeTagComboBox, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));

        return barLayout;
    }

    private void createPerson(String firstName, String lastName)
    {
        orgMgr.createPerson(firstName, lastName);
        set(orgMgr);
    }

    private void addTagToSelectedPeople()
    {
        OrgTag tag = addTagComboBox.getValue();
        if (tag == null) { return; }

        orgMgr.addTagId(tag.getId(), selectedPeople);
        set(); // TODO - overkill
    }

    private void removeTagFromSelectedPeople()
    {
        OrgTag tag = removeTagComboBox.getValue();
        if (tag == null) { return; }

        orgMgr.removeTagId(tag.getId(), selectedPeople);
        set(); // at least one person will have been updated // TODO - overkill
    }

    @Override
    public void selectionChange(MultiSelectionEvent<Person> event)
    {
        selectedPeople = new ArrayList<>(event.getAllSelectedItems());
        if (selectedPeople.isEmpty())
        {
            addTagComboBox.setEnabled(false);

            removeTagComboBox.setItems(Collections.emptyList());
            removeTagComboBox.setEnabled(false);
            return;
        }

        addTagComboBox.setEnabled(true);

        Set<String> selectedPeopledTagIds = new HashSet<>();
        for (Person person : selectedPeople)
        {
            selectedPeopledTagIds.addAll(person.getTagIds());
        }

        List<OrgTag> selectedOrgTags = selectedPeopledTagIds.stream()
            .filter(tagId -> orgTagsById.keySet().contains(tagId))
            .map(tagId -> orgTagsById.get(tagId))
            .collect(Collectors.toList());

        if (selectedOrgTags.isEmpty())
        {
            removeTagComboBox.setItems(Collections.emptyList());
            removeTagComboBox.setEnabled(false);
        }
        else
        {
            removeTagComboBox.setItems(selectedOrgTags);
            removeTagComboBox.setEnabled(true);
        }
    }

    class ImportPeoplePanel extends Panel
    {
        VerticalLayout layout = new VerticalLayout();

        TextArea textArea = new TextArea();

        public ImportPeoplePanel()
        {
            super("Add People by pasting contents of a csv below.  Sample file is ");

            textArea.setRows(20);
            textArea.setWidth("100%");

            Button button = new Button("Import");

            layout.addComponents(textArea, button);


            setContent(layout);
        }
    }


}