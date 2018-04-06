package xyz.cleangone.e2.web.vaadin.desktop.admin.event;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.selection.MultiSelectionEvent;
import com.vaadin.event.selection.MultiSelectionListener;
import com.vaadin.server.Setter;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.FooterRow;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.event.ItemManager;
import xyz.cleangone.e2.web.vaadin.util.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static xyz.cleangone.data.aws.dynamo.entity.base.BaseMixinEntity.NAME_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem.CATEGORIES_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem.PRICE_FIELD;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;


public class ItemsAdmin extends BaseEventTagsAdmin implements MultiSelectionListener<CatalogItem>
{
    private ItemManager itemMgr;

    private List<OrgTag> categories;
    private Map<String, OrgTag> categoriesById;

    private ComboBox<OrgTag> addCategoryComboBox = new ComboBox<>();
    private ComboBox<OrgTag> removeCategoryComboBox = new ComboBox<>();;
    private List<CatalogItem> selectedItems;

    public ItemsAdmin(EventsAdminLayout eventsAdminLayout, MessageDisplayer msgDisplayer)
    {
        super(eventsAdminLayout, OrgTag.TagType.Category, msgDisplayer);

        setSizeFull();
        setMargin(false);
        setSpacing(true);
        setWidth("100%");
    }

    public void set()
    {
        super.set();

        // todo - this is a list of all categories, not just ones available to event
        categories = tagMgr.getCategories();
        categoriesById = tagMgr.getTagsById(categories);
        itemMgr = eventMgr.getItemManager(); // todo - work on caching

        addCategoryComboBox.setItems(categories);
        addCategoryComboBox.setValue(null);  // todo - there is a bug with setValue that is being fixed
        removeCategoryComboBox.setValue(null);

        setContent();
    }

    public void setContent()
    {
        removeAllComponents();

        Component tagDisclosure = getTagDisclosure(new DescriptionGenerator("Category", "Categories"));
        Component grid = getItemsGrid();
        addComponents(tagDisclosure, getAddItemLayout(), grid, new Label());
        setExpandRatio(grid, 1.0f);
    }

    private void setContent(ItemAdmin itemAdmin)
    {
        removeAllComponents();
        addComponent(itemAdmin);
    }

    private Component getAddItemLayout()
    {
        GridLayout barLayout = new GridLayout(2, 1);
        barLayout.setSizeUndefined();
        barLayout.setWidth("100%");

        HorizontalLayout leftLayout = new HorizontalLayout();
        barLayout.addComponent(leftLayout);

        HorizontalLayout rightLayout = new HorizontalLayout();
        barLayout.addComponent(rightLayout);
        barLayout.setComponentAlignment(rightLayout, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));

        ComboBox<OrgTag> categoryComboBox = new ComboBox<>();
        categoryComboBox.addStyleName(ValoTheme.TEXTFIELD_TINY);
        categoryComboBox.setPlaceholder("Category");
        categoryComboBox.setItems(tagMgr.getTags(OrgTag.TagType.Category));
        categoryComboBox.setItemCaptionGenerator(OrgTag::getName);

        TextField nameField = VaadinUtils.createGridTextField("Name");

        Button button = new Button("Add Product");
        button.addStyleName(ValoTheme.TEXTFIELD_TINY);
        button.addClickListener(new Button.ClickListener()
        {
            @Override
            public void buttonClick(Button.ClickEvent event)
            {
                OrgTag category = categoryComboBox.getValue();
                String categoryId = category == null ? null : category.getId();
                itemMgr.createItem(nameField.getValue(), categoryId);

                msgDisplayer.displayMessage("Item added");
                set();
            }
        });

        leftLayout.addComponents(categoryComboBox, nameField, button);

        addCategoryComboBox.addStyleName(ValoTheme.TEXTFIELD_TINY);
        addCategoryComboBox.setPlaceholder("Add Category");
        addCategoryComboBox.setItemCaptionGenerator(OrgTag::getName);
        addCategoryComboBox.setEnabled(false);
        addCategoryComboBox.addValueChangeListener(event -> addCategoryToSelectedItems());

        removeCategoryComboBox.addStyleName(ValoTheme.TEXTFIELD_TINY);
        removeCategoryComboBox.setPlaceholder("Remove Category");
        removeCategoryComboBox.setItemCaptionGenerator(OrgTag::getName);
        removeCategoryComboBox.setEnabled(false);
        removeCategoryComboBox.addValueChangeListener(event -> removeCategoryFromSelectedItems());

        rightLayout.addComponents(new Label("Categories"), addCategoryComboBox, removeCategoryComboBox);
        rightLayout.setComponentAlignment(addCategoryComboBox, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));
        rightLayout.setComponentAlignment(removeCategoryComboBox, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));

        return barLayout;
    }

    private void addCategoryToSelectedItems()
    {
        OrgTag category = addCategoryComboBox.getValue();
        if (category == null) { return; }

        itemMgr.addCategoryId(category.getId(), selectedItems);
        set();
    }

    private void removeCategoryFromSelectedItems()
    {
        OrgTag category = removeCategoryComboBox.getValue();
        if (category == null) { return; }

        itemMgr.removeCategoryId(category.getId(), selectedItems);
        set();
    }

    private Grid<CatalogItem> getItemsGrid()
    {
        Grid<CatalogItem> grid = new Grid<>();
        grid.setSizeFull();

        Grid.Column nameCol = grid.addComponentColumn(this::buildNameLinkButton);
        nameCol.setId(NAME_FIELD.getName());
        addColumn(grid, CATEGORIES_FIELD, CatalogItem::getCategoriesCsv, 2);
        addColumn(grid, PRICE_FIELD, CatalogItem::getDisplayPrice, 1);

        //addBigDecimalColumn(grid, PRICE_FIELD, CatalogItem::getPrice, 1);


        DollarField dollars = new DollarField("Amount");

        grid.addComponentColumn(this::buildDeleteButton);

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

    private void setColumnFiltering(HeaderRow filterHeader, CountingDataProvider<CatalogItem> dataProvider)
    {
        MultiFieldFilter<CatalogItem> filter = new MultiFieldFilter<>(dataProvider);

        addFilterField(CATEGORIES_FIELD, CatalogItem::getName, filter, filterHeader);
        addFilterField(NAME_FIELD, CatalogItem::getCategoriesCsv, filter, filterHeader);
    }

    private void addFilterField(EntityField entityField,
        ValueProvider<CatalogItem, String> valueProvider, MultiFieldFilter<CatalogItem> filter, HeaderRow filterHeader)
    {
        filter.addField(entityField, valueProvider);
        VaadinUtils.addFilterField(entityField, filter, filterHeader);
    }

    private Component buildNameLinkButton(CatalogItem item)
    {
        Button nameLinkButton = VaadinUtils.createLinkButton(
            item.getName(), e -> setContent(new ItemAdmin(eventMgr.getItemManager(item), msgDisplayer, this, ui)));

        return nameLinkButton;
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
        if (selectedItems.isEmpty())
        {
            addCategoryComboBox.setEnabled(false);

            removeCategoryComboBox.setItems(Collections.emptyList());
            removeCategoryComboBox.setEnabled(false);
            return;
        }

        addCategoryComboBox.setEnabled(true);

        Set<String> selectedCategoryIds = new HashSet<>();
        for (CatalogItem selectedItem : selectedItems)
        {
            selectedCategoryIds.addAll(selectedItem.getCategoryIds());
        }

        List<OrgTag> selectedCategories = selectedCategoryIds.stream()
            .map(id -> categoriesById.get(id))
            .collect(Collectors.toList());

        removeCategoryComboBox.setItems(selectedCategories);
        removeCategoryComboBox.setEnabled(!selectedCategories.isEmpty());
    }
}