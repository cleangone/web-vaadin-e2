package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.tag;

import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.web.vaadin.ui.EntityGrid;
import xyz.cleangone.web.vaadin.ui.MessageDisplayer;
import xyz.cleangone.web.vaadin.util.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag.*;
import static xyz.cleangone.web.vaadin.util.VaadinUtils.*;

public class TagAdmin extends BaseTagAdmin
{
    private AddTagLayout addTagLayout = new AddTagLayout();

    private TagManager tagMgr;
    private EventManager eventMgr;
    private String tagTypeName;

    public TagAdmin(TagsAdminLayout tagsAdminLayout, MessageDisplayer msgDisplayer)
    {
        super(tagsAdminLayout, msgDisplayer);
        setLayout(this, MARGIN_T, SPACING_TRUE, BACK_GREEN);
    }

    public void set(SessionManager sessionMgr)
    {
        tagMgr = sessionMgr.getOrgManager().getTagManager();
        eventMgr = sessionMgr.getEventManager();
    }

    public void set()
    {
        tagTypeName = tagMgr.getTagType().getName();
        addTagLayout.setData();

        removeAllComponents();
        Component grid = new TagsGrid();
        addComponents(addTagLayout, grid, new Label());
        setExpandRatio(grid, 1.0f);
    }

    private class AddTagLayout extends HorizontalLayout
    {
        TextField addNameField = VaadinUtils.createGridTextField("Placeholder");
        Button button = new Button("Placeholder");

        AddTagLayout()
        {
            VaadinUtils.setLayout(this, VaadinUtils.SIZE_UNDEFINED);

            button.addStyleName(ValoTheme.TEXTFIELD_TINY);
            button.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    tagMgr.createTag(addNameField.getValue());
                    addNameField.setValue("");
                    set();
                }
            });

            addComponents(addNameField, button);
        }

        void setData()
        {
            addNameField.setPlaceholder(tagTypeName + " Name");
            button.setCaption("Add " + tagTypeName);
        }
    }

    private class TagsGrid extends EntityGrid<OrgTag>
    {
        TagsGrid()
        {
            setSizeFull();
            setWidth("75%");

            List<OrgEvent> events = eventMgr.getEvents();
            Map<String, OrgEvent> eventsById = events.stream().collect(Collectors.toMap(OrgEvent::getId, event -> event));
            Map<String, String> eventIdsByName = events.stream().collect(Collectors.toMap(OrgEvent::getName, OrgEvent::getId));

            // need to sort
            ComboBox<String> eventNameComboBox = new ComboBox<>();
            eventNameComboBox.setItems(eventIdsByName.keySet());

            Grid.Column<OrgTag, String> nameCol = addColumn(TAG_NAME_FIELD, OrgTag::getName, OrgTag::setName);
            addColumn(DISPLAY_ORDER_FIELD, OrgTag::getDisplayOrder, OrgTag::setDisplayOrder);
            addColumn(EVENT_NAME_FIELD, OrgTag::getEventName).setEditorComponent(eventNameComboBox, OrgTag::setEventName);
            addComponentColumn(this::buildDeleteButton).setWidth(ICON_COL_WIDTH);

            sort(nameCol, SortDirection.ASCENDING);

            setEditor(event -> {
                OrgTag tag = event.getBean();
                String eventName = tag.getEventName();
                tag.setEventId(eventName == null ? null : eventIdsByName.get(eventName));
                tagMgr.save(tag);
                set();
            });

            List<OrgTag> tags = tagMgr.getTags(tagTypeName, eventsById);
            CountingDataProvider<OrgTag> dataProvider = new CountingDataProvider<OrgTag>(tags, countLabel);
            setDataProvider(dataProvider);

            setColumnFiltering(appendHeaderRow(), dataProvider);
            appendCountFooterRow(TAG_NAME_FIELD);
        }

        private Button buildDeleteButton(OrgTag tag)
        {
            String desc = tagTypeName + " '" + tag.getName() + "'";
            return buildDeleteButton(tag, "Delete " + desc, "Confirm " + desc + " Delete", "Delete " + desc + "?");
        }

        @Override
        protected void delete(OrgTag tag)
        {
            tagMgr.delete(tag);
            set();
        }

        private void setColumnFiltering(HeaderRow filterHeader, CountingDataProvider<OrgTag> dataProvider)
        {
            MultiFieldFilter<OrgTag> filter = new MultiFieldFilter<>(dataProvider);
            addFilterField(TAG_NAME_FIELD, OrgTag::getName, filter, filterHeader);
        }
    }
}