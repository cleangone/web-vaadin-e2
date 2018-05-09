package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.server.Setter;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.util.BeanItem;
import org.vaadin.dialogs.ConfirmDialog;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.disclosure.BaseOrgDisclosure;
import xyz.cleangone.e2.web.vaadin.util.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static xyz.cleangone.data.aws.dynamo.entity.base.BaseMixinEntity.NAME_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag.DISPLAY_ORDER_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag.USER_VISIBLE_FIELD;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createDeleteButton;


public class TagsAdmin extends BaseEventAdmin
{
    private final OrgTag.TagType tagType;
    private final String tagTypeName;

    protected EventManager eventMgr;
    protected OrgManager orgMgr;
    protected TagManager tagMgr;
    protected OrgEvent event;

    protected TagsDisclosure tagsDisclosure;

    public TagsAdmin(EventsAdminLayout eventsAdminLayout, OrgTag.TagType tagType, MessageDisplayer msgDisplayer)
    {
        super(eventsAdminLayout, msgDisplayer);
        this.tagType = tagType;
        tagTypeName = TagManager.getSingularName(tagType);

        setSizeFull();

        if (showDisclosure()) { setMargin(false); }
        else { setMargin(new MarginInfo(true, false, false, false)); } // T/R/B/L

        setSpacing(true);
        setWidth("100%");
    }

    public void set(SessionManager sessionMgr)
    {
        eventMgr = sessionMgr.getEventManager();
        orgMgr = sessionMgr.getOrgManager();
        tagMgr = orgMgr.getTagManager();
    }

    public void set()
    {
        event = requireNonNull(eventMgr.getEvent());

        removeAllComponents();
        if (showDisclosure())
        {
            addComponent(getTagDisclosure(new DescriptionGenerator(tagTypeName, TagManager.getPluralName(tagType))));
        }

        Component grid = getTagsGrid();
        addComponents(getAddTagLayout(), grid, new Label());
        setExpandRatio(grid, 1.0f);
    }

    private boolean showDisclosure() { return tagType == OrgTag.TagType.PersonTag || tagType == OrgTag.TagType.Category; }

    private Component getAddTagLayout()
    {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeUndefined();
        layout.setWidth("100%");

        TextField addNameField = VaadinUtils.createGridTextField("Event " + tagTypeName + " Name");
        layout.addComponent(addNameField);

        Button button = new Button("Add " + tagTypeName);
        button.addStyleName(ValoTheme.TEXTFIELD_TINY);
        button.addClickListener(new Button.ClickListener()
        {
            @Override
            public void buttonClick(Button.ClickEvent ev)
            {
                tagMgr.createTag(addNameField.getValue(), tagType, event);
                msgDisplayer.displayMessage(tagTypeName + " added");
                set();
            }
        });

        layout.addComponents(addNameField, button);

        return layout;
    }

    private Grid<OrgTag> getTagsGrid()
    {
        Grid<OrgTag> grid = new Grid<>();
        grid.setSizeFull();

        List<Component> editComponents = new ArrayList<>();
        addColumn(grid, NAME_FIELD, OrgTag::getName, OrgTag::setName, editComponents);
        addColumn(grid, DISPLAY_ORDER_FIELD, OrgTag::getDisplayOrder, OrgTag::setDisplayOrder, editComponents);
        addBooleanColumn(grid, USER_VISIBLE_FIELD, OrgTag::getUserVisible, OrgTag::setUserVisible, editComponents);

        grid.addComponentColumn(this::buildDeleteButton);
        grid.setColumnReorderingAllowed(true);

        List orgWideIds = Collections.emptyList();
        if (tagType == OrgTag.TagType.PersonTag) { orgWideIds = event.getTagIds(); }
        else if (tagType == OrgTag.TagType.Category) { orgWideIds = event.getCategoryIds(); }
        List<OrgTag> tags = tagMgr.getEventTags(tagType, event.getId(), orgWideIds);
        grid.setDataProvider(new ListDataProvider<>(tags));

        grid.getEditor().setEnabled(true);
        grid.getEditor().setBuffered(true);
        grid.getEditor().addOpenListener(ev -> {
            OrgTag tag = ev.getBean();
            editComponents.forEach(c -> c.setEnabled(event.getId().equals(tag.getEventId())));
        });
        grid.getEditor().addSaveListener(ev -> {
            OrgTag tag = ev.getBean();
            if (event.getId().equals(tag.getEventId()))
            {
                tagMgr.save(tag);
                msgDisplayer.displayMessage("Tag saved");
                set();
            }
         });

        return grid;
    }

    private Grid.Column<OrgTag, String> addColumn(Grid<OrgTag> grid, EntityField entityField, ValueProvider<OrgTag, String> valueProvider)
    {
        return grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName());
    }

    private Grid.Column<OrgTag, String> addColumn(
        Grid<OrgTag> grid, EntityField entityField, ValueProvider<OrgTag, String> valueProvider, Setter<OrgTag, String> setter, List<Component> editComponents)
    {
        TextField textField = new TextField();
        editComponents.add(textField);
        return addColumn(grid, entityField, valueProvider).setEditorComponent(textField, setter);
    }

    private Grid.Column<OrgTag, Boolean> addBooleanColumn(
        Grid<OrgTag> grid, EntityField entityField, ValueProvider<OrgTag, Boolean> valueProvider, Setter<OrgTag, Boolean> setter, List<Component> editComponents)
    {
        CheckBox checkBox = new CheckBox();
        editComponents.add(checkBox);
        return grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName()).setEditorComponent(checkBox, setter);
    }

    private Button buildDeleteButton(OrgTag tag)
    {
        if (tag.getEventId() == null) { return null; }

        Button button = createDeleteButton("Delete Event Tag");
        button.addClickListener(e -> {
            ConfirmDialog.show(getUI(), "Confirm Delete",
                "Delete Event Tag '" + tag.getName() + "'?",
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

    protected Component getTagDisclosure(DescriptionGenerator descriptionGenerator)
    {
        tagsDisclosure = new TagsDisclosure(descriptionGenerator);
        return tagsDisclosure;
    }

    public void setOrgTagsDisclosureCaption() { tagsDisclosure.setDisclosureCaption(); }

    protected class TagsDisclosure extends BaseOrgDisclosure
    {
        DescriptionGenerator descGenerator;
        TagsDisclosure(DescriptionGenerator descGenerator)
        {
            super(new EventTagsAdmin(descGenerator), event);
            this.descGenerator = descGenerator;
            setDisclosureCaption();
        }

        public void setDisclosureCaption()
        {
            List<String> tagIds = eventMgr.getEventTagIds(tagType);
            setDisclosureCaption(descGenerator.numText(tagIds) + " organization-wide " + descGenerator.text(tagIds) + " visible to the Event");
        }
    }

    protected class EventTagsAdmin extends HorizontalLayout
    {
        final DescriptionGenerator descGenerator;
        List<CheckBoxGroup<OrgTag>> checkBoxGroups = new ArrayList<>();

        EventTagsAdmin(DescriptionGenerator descGenerator)
        {
            this.descGenerator = descGenerator;
            setMargin(false);
            setSpacing(true);

            // split tags into columns
            List<OrgTag> orgTags = tagMgr.getOrgTags(tagType);
            if (orgTags.isEmpty()) { return; }

            List<List<OrgTag>> tagCols = new ArrayList<>();
            List<OrgTag> tagCol = null;
            int colMaxSize = orgTags.size()/3 + 1;
            for (OrgTag tag : orgTags)
            {
                if (tagCol == null)
                {
                    tagCol = new ArrayList<>();
                    tagCols.add(tagCol);
                }

                tagCol.add(tag);
                if (tagCol.size() == colMaxSize) { tagCol = null; }
            }

            List<String> initialSelectedTagIds = eventMgr.getEventTagIds(tagType);
            for (List<OrgTag> tags : tagCols)
            {
                CheckBoxGroup<OrgTag> checkBoxGroup = getCheckBoxGroup(tags, initialSelectedTagIds);
                checkBoxGroups.add(checkBoxGroup);
                addComponent(checkBoxGroup);
            }
        }

        CheckBoxGroup<OrgTag> getCheckBoxGroup(List<OrgTag> orgTags, List<String> initialSelectedTagIds)
        {
            CheckBoxGroup<OrgTag> checkBoxGroup = new CheckBoxGroup<>();
            checkBoxGroup.setItems(orgTags);
            checkBoxGroup.setItemCaptionGenerator(OrgTag::getName);
            checkBoxGroup.addBlurListener(event -> saveTags());

            if (initialSelectedTagIds != null)
            {
                orgTags.stream()
                    .filter(tag -> initialSelectedTagIds.contains(tag.getId()))
                    .forEach(checkBoxGroup::select);
            }

            return checkBoxGroup;
        }

        void saveTags()
        {
            Set<OrgTag> selectedTags = new HashSet<>();
            checkBoxGroups.forEach(group -> selectedTags.addAll(group.getSelectedItems()));

            List<String> tagIds = selectedTags.stream()
                .map(OrgTag::getId)
                .collect(Collectors.toList());

            eventMgr.setEventTagIds(tagIds, tagType);
            eventMgr.save();

            msgDisplayer.displayMessage(descGenerator.plural() + " saved");
            setOrgTagsDisclosureCaption();
        }
    }

}