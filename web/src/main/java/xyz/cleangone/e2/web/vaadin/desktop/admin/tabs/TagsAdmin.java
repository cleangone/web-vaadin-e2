package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs;

import com.vaadin.data.ValueProvider;
import com.vaadin.server.Setter;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.FooterRow;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.TextRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.e2.web.vaadin.util.CountingDataProvider;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.MultiFieldFilter;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.List;
import java.util.Map;
import static xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag.*;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;


public class TagsAdmin extends VerticalLayout
{
    private final MessageDisplayer msgDisplayer;
    private final OrgTag.TagType tagType;
    private final String tagTypeName;

    private TagManager tagMgr;
    private EventManager eventMgr;

    public TagsAdmin(MessageDisplayer msgDisplayer, OrgTag.TagType tagType)
    {
        this.msgDisplayer = msgDisplayer;
        this.tagType = tagType;
        tagTypeName = TagManager.getSingularName(tagType);

        setWidth("100%");
        setHeight("100%");
        setMargin(new MarginInfo(true, false, true, false)); // T/R/B/L
        setSpacing(true);
    }

    public void set(TagManager tagMgr, EventManager eventMgr)
    {
        this.tagMgr = tagMgr;
        this.eventMgr = eventMgr;
        set();
    }

    private void set()
    {
        removeAllComponents();

        addComponent(getTopBarLayout());

        Component grid = getTagGrid();
        addComponent(grid);
        setExpandRatio(grid, 1.0f);
    }

    private Component getTopBarLayout()
    {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeUndefined();

        TextField addNameField = VaadinUtils.createGridTextField(tagTypeName + " Name");
        layout.addComponent(addNameField);

        Button button = new Button("Add " + tagTypeName);
        button.addStyleName(ValoTheme.TEXTFIELD_TINY);
        layout.addComponent(button);
        button.addClickListener(new Button.ClickListener()
        {
            @Override
            public void buttonClick(Button.ClickEvent event)
            {
                tagMgr.createTag(addNameField.getValue(), tagType);
                set();
            }
        });

        return layout;
    }

    private Grid<OrgTag> getTagGrid()
    {
        Map<String, OrgEvent> getEventsById = eventMgr.getEventsById();

        ComboBox<OrgEvent> eventComboBox = new ComboBox<>("Select Event");
        eventComboBox.setItems(getEventsById.values());
        eventComboBox.setItemCaptionGenerator(OrgEvent::getName);

        Grid<OrgTag> grid = new Grid<>();
        grid.setSizeFull();

        Grid.Column<OrgTag, String> nameCol = addColumn(grid, TAG_NAME_FIELD, OrgTag::getName, OrgTag::setName);
        addColumn(grid, DISPLAY_ORDER_FIELD, OrgTag::getDisplayOrder, OrgTag::setDisplayOrder);
        addBooleanColumn(grid, USER_VISIBLE_FIELD, OrgTag::getUserVisible, OrgTag::setUserVisible);
        addColumn(grid, EVENT_NAME_FIELD, OrgTag::getEventName);
        //addColumn(grid, NOTES_FIELD, OrgTag::getNotes, OrgTag::setNotes);

        Grid.Column<OrgTag, Component> actionCol =
            grid.addComponentColumn(this::buildDeleteButton);
        actionCol.setStyleGenerator(item -> "v-align-middle"); // doesn't work

        grid.sort(nameCol, SortDirection.ASCENDING);

        grid.getEditor().setEnabled(true);
        grid.getEditor().addSaveListener(event -> {
            OrgTag tag = event.getBean();
            tagMgr.save(tag);
            set();
        });

        List<OrgTag> tags = tagMgr.getTags(tagType, getEventsById);
        Label countLabel = new Label();
        CountingDataProvider<OrgTag> dataProvider = new CountingDataProvider<OrgTag>(tags, countLabel);
        grid.setDataProvider(dataProvider);

        HeaderRow filterHeader = grid.appendHeaderRow();
        setColumnFiltering(filterHeader, dataProvider);

        FooterRow footerRow = grid.appendFooterRow();
        footerRow.getCell(TAG_NAME_FIELD.getName()).setComponent(countLabel);

        return grid;
    }

    private Button buildDeleteButton(OrgTag tag)
    {
        Button button = createDeleteButton("Delete " + tagTypeName);
        button.addClickListener(e -> {
            ConfirmDialog.show(getUI(),
                "Confirm " + tagTypeName +" Delete",
                "Delete " + tagTypeName + " '" + tag.getName() + "'?",
                "Delete", "Cancel", new ConfirmDialog.Listener() {
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        tagMgr.delete(tag);
                        set();
                    }
                }
            });
        });

        return button;
    }

    private Grid.Column<OrgTag, String> addColumn(Grid<OrgTag> grid, EntityField entityField, ValueProvider<OrgTag, String> valueProvider)
    {
        return grid.addColumn(valueProvider)
            .setId(entityField.getName())
            .setCaption(entityField.getDisplayName())
            .setExpandRatio(1);
    }

    private Grid.Column<OrgTag, String> addColumn(
        Grid<OrgTag> grid, EntityField entityField,
        ValueProvider<OrgTag, String> valueProvider, Setter<OrgTag, String> setter)
    {
        return addColumn(grid, entityField, valueProvider)
            .setEditorComponent(new TextField(), setter);
    }

    private void addBooleanColumn(
        Grid<OrgTag> grid, EntityField entityField, ValueProvider<OrgTag, Boolean> valueProvider, Setter<OrgTag, Boolean> setter)
    {
        grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName())
            .setEditorComponent(new CheckBox(), setter);
    }

    private void setColumnFiltering(HeaderRow filterHeader, CountingDataProvider<OrgTag> dataProvider)
    {
        MultiFieldFilter<OrgTag> filter = new MultiFieldFilter<>(dataProvider);
        addFilterField(TAG_NAME_FIELD, OrgTag::getName, filter, filterHeader);
    }

    private void addFilterField(
        EntityField entityField, ValueProvider<OrgTag, String> valueProvider, MultiFieldFilter<OrgTag> filter, HeaderRow filterHeader)
    {
        filter.addField(entityField, valueProvider);
        VaadinUtils.addFilterField(entityField, filter, filterHeader);
    }


}