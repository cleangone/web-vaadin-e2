package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.tag;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.lastTouched.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.organization.TagType;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.web.vaadin.ui.EntityGrid;
import xyz.cleangone.web.vaadin.ui.MessageDisplayer;
import xyz.cleangone.web.vaadin.util.VaadinUtils;

import java.util.List;
import java.util.stream.Collectors;

import static xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent.*;
import static xyz.cleangone.data.aws.dynamo.entity.organization.TagType.*;
import static xyz.cleangone.web.vaadin.util.VaadinUtils.*;

public class TagTypesAdmin extends BaseTagAdmin
{
    private AddTagTypeLayout addTagTypeLayout = new AddTagTypeLayout();
    private TagTypeGrid tagTypeGrid = new TagTypeGrid();

    private TagManager tagMgr;

    public TagTypesAdmin(TagsAdminLayout tagsAdminLayout, MessageDisplayer msgDisplayer)
    {
        super(tagsAdminLayout, msgDisplayer);
        setLayout(this, MARGIN_T, SPACING_TRUE, BACK_GREEN);

        addComponents(addTagTypeLayout, tagTypeGrid);
        setExpandRatio(tagTypeGrid, 1.0f);
    }

    public void set(SessionManager sessionMgr)
    {
        tagMgr = sessionMgr.getOrgManager().getTagManager();
    }
    public void set()
    {
        tagsAdminLayout.setNav();
        tagTypeGrid.setData();
    }

    private class AddTagTypeLayout extends HorizontalLayout
    {
        AddTagTypeLayout()
        {
            VaadinUtils.setLayout(this, VaadinUtils.SIZE_UNDEFINED);

            TextField nameField = VaadinUtils.createGridTextField("Item Tag Type Name");
            Button button = createTextButton("Add Tag Type", e -> {
                tagMgr.createTagType(nameField.getValue(), EntityType.Item);
                nameField.setValue("");
                set();
            });

            addComponents(nameField, button);
        }
    }

    private class TagTypeGrid extends EntityGrid<TagType>
    {
        TagTypeGrid()
        {
            setSizeFull();
            setWidth("75%");

            addColumn(NAME_FIELD, TagType::getName, TagType::setName);
            addColumn(ENTITY_TYPE_FIELD, TagType::getEntityTypeString);
            addSortColumn(TagType.DISPLAY_ORDER_FIELD, TagType::getDisplayOrder, TagType::setDisplayOrder);
            addComponentColumn(this::buildDeleteButton).setWidth(ICON_COL_WIDTH);

            setEditor(ev -> {
                TagType tagType = ev.getBean();
                tagMgr.save(tagType);
                msgDisplayer.displayMessage("TagType saved");
                set();
            });
        }

        void setData()
        {
            // creating entirely new dataProvider because resetting items doesn't seem to work
            List<TagType> tagTypes = tagMgr.getTagTypes().stream()
                //.filter(t -> !t.isDefaultTagType())
                .collect(Collectors.toList());
            setDataProvider(new ListDataProvider<>(tagTypes));

            markAsDirty();
        }

        private Button buildDeleteButton(TagType tagType)
        {
            Button button = createDeleteButton("Delete " + tagType.getName());

            List<OrgTag> tagsWithTagType = tagMgr.getTags(tagType.getName());
            if (tagsWithTagType.isEmpty())
            {
                addDeleteClickListener(tagType, button, "Confirm " + tagType.getName() + " Delete", "Delete TagType '" + tagType.getName() + "'?");
            }
            else { button.setEnabled(false); }

            return button;
        }

        @Override
        protected void delete(TagType tagType)
        {
            tagMgr.delete(tagType);
            set();
        }
    }
}