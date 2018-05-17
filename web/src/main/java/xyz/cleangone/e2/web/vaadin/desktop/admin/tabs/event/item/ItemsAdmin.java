package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.item;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.selection.MultiSelectionEvent;
import com.vaadin.event.selection.MultiSelectionListener;
import com.vaadin.server.Setter;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.FooterRow;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.item.SaleStatus;
import xyz.cleangone.data.aws.dynamo.entity.item.SaleType;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.event.ItemManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.BaseEventTagsAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.EventsAdminLayout;
import xyz.cleangone.e2.web.vaadin.util.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static xyz.cleangone.data.aws.dynamo.entity.base.BaseMixinEntity.NAME_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem.*;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class ItemsAdmin extends BaseEventTagsAdmin implements MultiSelectionListener<CatalogItem>
{
    protected static String ADMIN_GRID_STYLE_NAME = "admin";
    private static boolean COLORS = false;

    private ItemManager itemMgr;
    private List<OrgTag> categories;
    private Map<String, OrgTag> categoriesById;
    private Map<EntityField, String> previousFilterValues = new HashMap<>();

    private final ItemMenuBar itemMenuBar;
    private List<CatalogItem> selectedItems;

    private Map<EntityField, String> filterValues;


    // pass eventsAdminLayout so msg can be sent to nav col
    public ItemsAdmin(EventsAdminLayout eventsAdminLayout, MessageDisplayer msgDisplayer)
    {
        super(eventsAdminLayout, OrgTag.TagType.Category, msgDisplayer);
        itemMenuBar = new ItemMenuBar(this);

        setSizeFull();
        setMargin(false);
        setSpacing(true);
        setWidth("100%");
        if (COLORS) { addStyleName("backGreen"); }
    }

    public void set(SessionManager sessionMgr)
    {
        filterValues = new HashMap<>();
        super.set(sessionMgr);
    }

    public void set()
    {
        super.set();

        event.getCategoryIds();
        categories = tagMgr.getCategories().stream()
            .filter(category -> event.getId().equals(category.getEventId()) || event.getCategoryIds().contains(category.getId()) )
            .collect(Collectors.toList());
        categoriesById = tagMgr.getTagsById(categories);
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

        Component grid = getItemsGrid();
        itemMenuBar.setItemsSelected(false);
        itemMenuBar.setCategories(categories);
        itemMenuBar.setAddCategories(null);
        itemMenuBar.setRemoveCategories(null);

        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.setMargin(false);
        gridLayout.setSpacing(true);
        gridLayout.addStyleName("marginLeft"); // todo - marginRight doesn't work, menu stack is collapsed to vertical ...
        gridLayout.addComponents(grid);
        gridLayout.setSizeFull();
        if (COLORS) { gridLayout.addStyleName("backYellow"); }

        addComponents(itemMenuBar, gridLayout);
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

    void addCategoryToSelectedItems(OrgTag category)
    {
        itemMgr.addCategoryId(category.getId(), selectedItems);
        set();
    }

    void removeCategoryFromSelectedItems(OrgTag category)
    {
        itemMgr.removeCategoryId(category.getId(), selectedItems);
        set();
    }

    private Grid<CatalogItem> getItemsGrid()
    {
        Grid<CatalogItem> grid = new Grid<>();
        grid.setStyleGenerator(item -> ADMIN_GRID_STYLE_NAME);
        grid.setSizeFull();

        Grid.Column<CatalogItem, LinkButton> nameCol = grid.addComponentColumn(this::buildNameLinkButton);
        nameCol.setId(NAME_FIELD.getName());
        nameCol.setComparator((link1, link2) -> link1.getName().compareTo(link2.getName()));

        addColumn(grid, CATEGORIES_FIELD, CatalogItem::getCategoriesCsv, 2);
        addColumn(grid, PRICE_FIELD, CatalogItem::getDisplayPrice, 1);
        addColumn(grid, COMBINED_STATUS_FIELD, CatalogItem::getCombinedStatus, 1);
        addDateColumn(grid, AVAIL_START_FIELD, CatalogItem::getAvailabilityStart, 1);
        Grid.Column endDateCol = addDateColumn(grid, AVAIL_END_FIELD, CatalogItem::getAvailabilityEnd, 1);
        Grid.Column deleteCol = grid.addComponentColumn(this::buildDeleteButton);

        grid.setColumnReorderingAllowed(true);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.asMultiSelect().addSelectionListener(this);

        List<CatalogItem> items = itemMgr.getItems();
        items.forEach(item -> item.setCategoriesCsv(categoriesById));

        Label countLabel = new Label();
        CountingDataProvider<CatalogItem> dataProvider = new CountingDataProvider<>(items, countLabel);
        grid.setDataProvider(dataProvider);

        grid.getEditor().setEnabled(true);
        grid.getEditor().setBuffered(true);
        grid.getEditor().addSaveListener(ev -> {
            CatalogItem item = ev.getBean();
            itemMgr.save(item);
            msgDisplayer.displayMessage("Item saved");
            grid.setDataProvider(new ListDataProvider<>(items));
        });

        grid.getColumns().stream()
            .filter(col -> col != nameCol && col != deleteCol)
            .forEach(column -> column.setHidable(true));
        endDateCol.setHidden(true);

        HeaderRow filterHeader = grid.appendHeaderRow();
        setColumnFiltering(filterHeader, dataProvider);

        FooterRow footerRow = grid.appendFooterRow();
        footerRow.getCell(NAME_FIELD.getName()).setComponent(countLabel);

        return grid;
    }

    private Grid.Column<CatalogItem, String> addColumn(
        Grid<CatalogItem> grid, EntityField entityField,
        ValueProvider<CatalogItem, String> valueProvider, Setter<CatalogItem, String> setter, int expandRatio)
    {
        return addColumn(grid, entityField, valueProvider, expandRatio).setEditorComponent(new TextField(), setter);
    }

    private Grid.Column<CatalogItem, String> addColumn(
        Grid<CatalogItem> grid, EntityField entityField, ValueProvider<CatalogItem, String> valueProvider, int expandRatio)
    {
        return grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName()).setExpandRatio(expandRatio);
    }

    private Grid.Column<CatalogItem, Boolean> addBooleanColumn(Grid<CatalogItem> grid,
        EntityField entityField, ValueProvider<CatalogItem, Boolean> valueProvider, int expandRatio)
    {
        return grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName()).setExpandRatio(expandRatio);
    }

    private Grid.Column<CatalogItem, BigDecimal> addBigDecimalColumn(Grid<CatalogItem> grid,
        EntityField entityField, ValueProvider<CatalogItem, BigDecimal> valueProvider, int expandRatio)
    {
        return grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName()).setExpandRatio(expandRatio);
    }

    private Grid.Column<CatalogItem, Date> addDateColumn(Grid<CatalogItem> grid,
        EntityField entityField, ValueProvider<CatalogItem, Date> valueProvider, int expandRatio)
    {
        return grid.addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName())
            .setRenderer(new DateRenderer(PageUtils.SDF_ADMIN_GRID))
            .setExpandRatio(expandRatio);
    }

    private void setColumnFiltering(HeaderRow filterHeader, CountingDataProvider<CatalogItem> dataProvider)
    {
        MultiFieldFilter<CatalogItem> filter = new MultiFieldFilter<>(dataProvider);

        addFilterField(NAME_FIELD,            CatalogItem::getName,           filter, filterHeader);
        addFilterField(COMBINED_STATUS_FIELD, CatalogItem::getCombinedStatus, filter, filterHeader);
        addFilterField(CATEGORIES_FIELD,      CatalogItem::getCategoriesCsv,  filter, filterHeader);

        previousFilterValues = filter.getFilterValues();
    }

    private void addFilterField(EntityField entityField,
        ValueProvider<CatalogItem, String> valueProvider, MultiFieldFilter<CatalogItem> filter, HeaderRow filterHeader)
    {
        filter.addField(entityField, valueProvider);
        TextField filterField = VaadinUtils.addFilterField(entityField, filter, filterHeader);

        if (previousFilterValues.containsKey(entityField)) { filterField.setValue(previousFilterValues.get(entityField)); }
    }

    private LinkButton buildNameLinkButton(CatalogItem item)
    {
        LinkButton linkButton = new LinkButton(item);
        linkButton.addClickListener(e -> setContent(new ItemAdmin(eventMgr.getItemManager(item), msgDisplayer, this, ui)));
        return linkButton;
    }

    private Component buildDeleteButton(CatalogItem item)
    {
        return createDeleteButton("Delete '" + item.getName() + "'", getUI(),
            new ConfirmDialog.Listener() { public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    itemMgr.delete(item);
                    set();
                }
            }
        });
    }

    @Override
    public void selectionChange(MultiSelectionEvent<CatalogItem> selectionEvent)
    {
        selectedItems = new ArrayList<>(selectionEvent.getAllSelectedItems());
        itemMenuBar.setItemsSelected(!selectedItems.isEmpty());

        if (selectedItems.isEmpty())
        {
            itemMenuBar.setAddCategories(null);
            itemMenuBar.setRemoveCategories(null);
            return;
        }

        itemMenuBar.setAddCategories(categories);

        Set<String> selectedCategoryIds = new HashSet<>();
        for (CatalogItem selectedItem : selectedItems)
        {
            selectedCategoryIds.addAll(selectedItem.getCategoryIds());
        }

        List<OrgTag> selectedCategories = selectedCategoryIds.stream()
            .map(id -> categoriesById.get(id))
            .collect(Collectors.toList());

        itemMenuBar.setRemoveCategories(selectedCategories);
    }

    class LinkButton extends Button
    {
        String name;

        LinkButton(CatalogItem item)
        {
            super(item.getName());
            name = item.getName();
            addStyleName(ValoTheme.BUTTON_LINK);
        }

        String getName()
        {
            return name;
        }
    }
}