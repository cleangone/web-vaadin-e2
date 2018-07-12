package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.item;

import com.vaadin.data.ValueProvider;
import com.vaadin.event.selection.MultiSelectionEvent;
import com.vaadin.event.selection.MultiSelectionListener;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.ColumnVisibilityChangeListener;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.item.SaleStatus;
import xyz.cleangone.data.aws.dynamo.entity.item.SaleType;
import xyz.cleangone.data.aws.dynamo.entity.lastTouched.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.organization.TagType;
import xyz.cleangone.data.manager.event.ItemManager;
import xyz.cleangone.web.vaadin.util.PageUtils;
import xyz.cleangone.web.vaadin.ui.EntityGrid;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.BaseEventTagsAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.EventsAdminLayout;
import xyz.cleangone.web.vaadin.ui.MessageDisplayer;
import xyz.cleangone.web.vaadin.util.*;

import java.util.*;
import java.util.stream.Collectors;

import static xyz.cleangone.data.aws.dynamo.entity.base.BaseNamedEntity.*;
import static xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem.*;
import static xyz.cleangone.web.vaadin.util.VaadinUtils.*;

public class ItemsAdmin extends BaseEventTagsAdmin implements MultiSelectionListener<CatalogItem>
{
    private ItemManager itemMgr;
    private List<OrgTag> categories;
    private Map<EntityField, String> previousFilterValues = new HashMap<>();
    private Map<String, Boolean> previousHiddenValues = new HashMap<>();

    private final ItemsMenuBar itemsMenuBar;
    private List<CatalogItem> selectedItems;

    private List<TagType> itemTagTypes;
    private List<OrgTag> itemTags;
    private Map<String, OrgTag> itemTagsById;

    // pass eventsAdminLayout so msg can be sent to nav col
    public ItemsAdmin(EventsAdminLayout eventsAdminLayout, MessageDisplayer msgDisplayer)
    {
        super(eventsAdminLayout, TagType.CATEGORY_TAG_TYPE, msgDisplayer);
        itemsMenuBar = new ItemsMenuBar(this);

        setLayout(this, MARGIN_FALSE, SPACING_TRUE, SIZE_FULL, BACK_GREEN);
    }

    public void set()
    {
        super.set();

        categories = tagMgr.getCategories().stream()
            .filter(category -> category.getEventId() == null || category.getEventId().equals(event.getId()))
            .collect(Collectors.toList());

        itemTagTypes = tagMgr.getTagTypes().stream()
            .filter(t -> t.isEntityType(EntityType.Item))
            .collect(Collectors.toList());

        itemTags = tagMgr.getTags(EntityType.Item);
        itemTagsById = tagMgr.getTagsById(itemTags);

        itemMgr = eventMgr.getItemManager(); // todo - work on caching

        setContent();
    }

    public ItemManager getItemManager()
    {
        return itemMgr;
    }

    public void setContent()
    {
        removeAllComponents();

        itemsMenuBar.setItemsSelected(false);
        itemsMenuBar.setCategories(categories);
        itemsMenuBar.clearTags();

        Component grid = new ItemsGrid();
        VerticalLayout gridLayout = vertical(grid, MARGIN_LR, SPACING_TRUE, SIZE_FULL, BACK_PINK);

        addComponents(itemsMenuBar, gridLayout);
        setExpandRatio(gridLayout, 1.0f);
    }

    private void setContent(ItemAdmin itemAdmin)
    {
        removeAllComponents();
        addComponent(itemAdmin);
    }

    void addItem(String name)
    {
        itemMgr.createItem(name, null);
        msgDisplayer.displayMessage("Item added");
        set();
    }

    void setSelectedItems(EntityField field, Date date)
    {
        for (CatalogItem item : selectedItems)
        {
            item.setDate(field, date);
            itemMgr.save(item);
        }
        set();
    }

    void setSelectedItems(SaleType saleType)
    {
        for (CatalogItem item : selectedItems)
        {
            item.setSaleType(saleType);
            itemMgr.save(item);
        }
        set();
    }

    void setSelectedItems(SaleStatus status)
    {
        for (CatalogItem item : selectedItems)
        {
            item.setSaleStatus(status);
            itemMgr.save(item);
        }
        set();
    }

    void addTagToSelectedItems(TagType tagType, OrgTag tag)
    {
        if (tagType.isTagType(TagType.CATEGORY_TAG_TYPE)) { itemMgr.addCategoryId(tag.getId(), selectedItems); }
        else { itemMgr.addTagId(tag.getId(), selectedItems); }

        set();
    }

    void removeTagFromSelectedItems(TagType tagType, OrgTag tag)
    {
        if (tagType.isTagType(TagType.CATEGORY_TAG_TYPE)) { itemMgr.removeCategoryId(tag.getId(), selectedItems); }
        else { itemMgr.removeTagId(tag.getId(), selectedItems); }

        set();
    }

    @Override
    public void selectionChange(MultiSelectionEvent<CatalogItem> selectionEvent)
    {
        selectedItems = new ArrayList<>(selectionEvent.getAllSelectedItems());
        itemsMenuBar.setItemsSelected(!selectedItems.isEmpty());
        itemsMenuBar.clearTags();

        if (selectedItems.isEmpty())
        {
            itemsMenuBar.clearTags();
            return;
        }

        Set<String> selectedTagIds = new HashSet<>();
        for (CatalogItem selectedItem : selectedItems)
        {
            selectedTagIds.addAll(selectedItem.getCategoryIds());
            selectedTagIds.addAll(selectedItem.getTagIds());

        }

        for (TagType itemTagType : itemTagTypes)
        {
            List<OrgTag> tags = itemTags.stream()
                .filter(t -> t.isTagType(itemTagType.getName()))
                .collect(Collectors.toList());

            List<OrgTag> selectedTags = selectedTagIds.stream()
                .map(id -> itemTagsById.get(id))
                .filter(t -> t.isTagType(itemTagType.getName()))
                .collect(Collectors.toList());

            itemsMenuBar.setTags(itemTagType, tags, selectedTags);
        }
    }

    private class ItemsGrid extends EntityGrid<CatalogItem> implements ColumnVisibilityChangeListener
    {
        ItemsGrid()
        {
            setStyleGenerator(item -> ADMIN_GRID_STYLE_NAME);
            setSizeFull();

            Grid.Column<CatalogItem, LinkButton> nameCol = addComponentColumn(this::buildNameLinkButton);
            nameCol.setId(NAME_FIELD.getName());
            nameCol.setComparator((link1, link2) -> link1.getName().compareTo(link2.getName()));

            addColumn(CATEGORIES_FIELD, CatalogItem::getCategoriesCsv);
            addColumn(PRICE_FIELD, CatalogItem::getDisplayPrice);
            addColumn(COMBINED_STATUS_FIELD, CatalogItem::getCombinedStatus);
            addDateColumn(AVAIL_START_FIELD, CatalogItem::getAvailabilityStart, PageUtils.SDF_ADMIN_GRID);
            Grid.Column endDateCol = addDateColumn(AVAIL_END_FIELD, CatalogItem::getAvailabilityEnd, PageUtils.SDF_ADMIN_GRID);
            Grid.Column deleteCol = addComponentColumn(this::buildDeleteButton);

            for (TagType itemTagType : itemTagTypes)
            {
                if (!itemTagType.isDefaultTagType())
                {
                    Boolean previousHidden = previousHiddenValues.get(itemTagType.getName());
                    addColumn(catalogItem -> catalogItem.getTagsCsv(itemTagType.getId()))
                        .setId(itemTagType.getName())
                        .setCaption(itemTagType.getName())
                        .setHidden(previousHidden == null ? true : previousHidden);
                }
            }

            addColumnVisibilityChangeListener(this);

            setColumnReorderingAllowed(true);
            setSelectionMode(Grid.SelectionMode.MULTI);
            asMultiSelect().addSelectionListener(ItemsAdmin.this);

            List<CatalogItem> items = itemMgr.getItems();
            for (CatalogItem item : items)
            {
                item.setCategorieCsv(itemTagsById);
                for (TagType itemTagType : itemTagTypes)
                {
                    item.setTagsCsv(itemTagType.getId(), itemTagsById);
                }
            }

            CountingDataProvider<CatalogItem> dataProvider = new CountingDataProvider<>(items, countLabel);
            setDataProvider(dataProvider);

            getColumns().stream()
                .filter(col -> col != nameCol && col != deleteCol)
                .forEach(column -> column.setHidable(true));
            endDateCol.setHidden(true);

            setColumnFiltering(dataProvider);
            appendCountFooterRow(NAME_FIELD);
        }

        private LinkButton buildNameLinkButton(CatalogItem item)
        {
            return new LinkButton(item.getName(), e -> setContent(new ItemAdmin(eventMgr.getItemManager(item), msgDisplayer, ItemsAdmin.this, ui)));
        }

        private Component buildDeleteButton(CatalogItem item)
        {
            return super.buildDeleteButton(item, item.getName());
        }


        public void columnVisibilityChanged(Grid.ColumnVisibilityChangeEvent event)
        {
            previousHiddenValues.put(event.getColumn().getId(), event.isHidden());
        }


        @Override
        protected void delete(CatalogItem item)
        {
            itemMgr.delete(item);
            set();
        }

        @Override
        protected void setColumnFiltering(MultiFieldFilter<CatalogItem> filter, HeaderRow filterHeader, CountingDataProvider<CatalogItem> dataProvider)
        {
            addFilterWithPrev(NAME_FIELD,            CatalogItem::getName,           filter, filterHeader);
            addFilterWithPrev(COMBINED_STATUS_FIELD, CatalogItem::getCombinedStatus, filter, filterHeader);
            addFilterWithPrev(CATEGORIES_FIELD,      CatalogItem::getCategoriesCsv,  filter, filterHeader);

            previousFilterValues = filter.getFilterValues();
        }

        protected void addFilterWithPrev(EntityField entityField, ValueProvider<CatalogItem, String> valueProvider, MultiFieldFilter<CatalogItem> filter, HeaderRow filterHeader)
        {
            TextField filterField = addFilterField(entityField, valueProvider, filter, filterHeader);
            if (previousFilterValues.containsKey(entityField)) { filterField.setValue(previousFilterValues.get(entityField)); }
        }
    }

    class LinkButton extends Button
    {
        String name;

        LinkButton(String name, ClickListener listener)
        {
            super(name);
            this.name = name;
            addStyleName(ValoTheme.BUTTON_LINK);

            addClickListener(listener);
        }

        String getName()
        {
            return name;
        }
    }

}