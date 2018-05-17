package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.item;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.item.SaleStatus;
import xyz.cleangone.data.aws.dynamo.entity.item.SaleType;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.*;
import xyz.cleangone.e2.web.vaadin.util.PageUtils;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.Date;
import java.util.List;

public class ItemMenuBar extends BaseActionBar
{
    private final ItemsAdmin itemsAdmin;
    private MyLeftMenuBar leftMenuBar;
    private MyCenterMenuBar centerMenuBar = new MyCenterMenuBar();


    public ItemMenuBar(ItemsAdmin itemsAdmin)
    {
        this.itemsAdmin = itemsAdmin;
        leftMenuBar = new MyLeftMenuBar(itemsAdmin);

        HorizontalLayout leftLayout = getLayout(leftMenuBar, "40%");
        HorizontalLayout centerLayout = getLayout(centerMenuBar, "50%");

        addPopup(leftMenuBar.getAddItemPopup(), leftLayout);
        addPopup(leftMenuBar.getUploadItemPopup(), leftLayout);

        PopupView startDatePopup = centerMenuBar.getStartDatePopup();
        PopupView endDatePopup = centerMenuBar.getEndDatePopup();
        centerLayout.addComponents(startDatePopup, endDatePopup);
        centerLayout.setComponentAlignment(startDatePopup, new Alignment(AlignmentInfo.Bits.ALIGNMENT_BOTTOM));
        centerLayout.setComponentAlignment(endDatePopup, new Alignment(AlignmentInfo.Bits.ALIGNMENT_BOTTOM));

        addComponents(leftLayout, centerLayout);
    }

    private void addPopup(PopupView popup, HorizontalLayout layout)
    {
        layout.addComponent(popup);
        layout.setComponentAlignment(popup, new Alignment(AlignmentInfo.Bits.ALIGNMENT_BOTTOM));
    }

    public void setItemsSelected(boolean itemsSelected)
    {
        centerMenuBar.setItemsSelected(itemsSelected);
    }

    public void setCategories(List<OrgTag> categories)
    {
        leftMenuBar.setCategories(categories);
    }
    public void setAddCategories(List<OrgTag> categories)
    {
        centerMenuBar.setAddCategories(categories);
    }
    public void setRemoveCategories(List<OrgTag> categories)
    {
        centerMenuBar.setRemoveCategories(categories);
    }

    class MyLeftMenuBar extends BaseMenuBar
    {
        MenuBar.MenuItem addItem;
        MenuBar.MenuItem uploadItem;
        UploadPopup uploadPopup;
        PopupView addItemPopup;
        PopupView uploadItemPopup;

        public MyLeftMenuBar(ItemsAdmin itemsAdmin)
        {
            MenuBar.MenuItem menuItem = addItem("",  VaadinIcons.PLUS, null);
            menuItem.setStyleName("icon-only");

            addItemPopup = new PopupView(null, createAddItemPopupLayout());
            addItem = menuItem.addItem("Add Item", null, new MenuBar.Command() {
                public void menuSelected(MenuBar.MenuItem selectedItem)
                {
                    addItemPopup.setPopupVisible(true);
                }
            });

            uploadPopup = new UploadPopup(itemsAdmin);
            uploadItemPopup = new PopupView(null, uploadPopup);
            uploadItemPopup.setHideOnMouseOut(false);
            uploadItem = menuItem.addItem("Upload Items/Images", null, new MenuBar.Command() {
                public void menuSelected(MenuBar.MenuItem selectedItem)
                {
                    uploadItemPopup.setPopupVisible(true);
                }
            });
        }

        public void setCategories(List<OrgTag> categories)
        {
            uploadPopup.setCategories(categories);
        }
        public PopupView getAddItemPopup()
        {
            return addItemPopup;
        }
        public PopupView getUploadItemPopup()
        {
            return uploadItemPopup;
        }

        private Component createAddItemPopupLayout()
        {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setMargin(true);
            layout.setSpacing(true);
            layout.setSizeUndefined();

            TextField nameField = VaadinUtils.createGridTextField("Name");

            Button button = new Button("Add Item");
            button.addStyleName(ValoTheme.TEXTFIELD_TINY);
            button.addClickListener(new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event)
                {
                    itemsAdmin.addItem(nameField.getValue());
                }
            });

            layout.addComponents(nameField, button);
            return layout;
        }

        private Component createUploadItemPopupLayout()
        {
            return new UploadPopup(itemsAdmin);
        }
    }

    class MyCenterMenuBar extends MenuBar
    {
        MenuBar.MenuItem dateItem;
        MenuBar.MenuItem categoryItem;
        MenuBar.MenuItem addCategoryItem;
        MenuBar.MenuItem removeCategoryItem;
        MenuBar.MenuItem statusItem;

        PopupView startDatePopup;
        PopupView endDatePopup;

        public MyCenterMenuBar()
        {
            addStyleName(ValoTheme.MENUBAR_BORDERLESS);

            startDatePopup = new PopupView(null, createDatePopupLayout(CatalogItem.AVAIL_START_FIELD));
            endDatePopup   = new PopupView(null, createDatePopupLayout(CatalogItem.AVAIL_END_FIELD));

            MenuBar.MenuItem menuItem = addItem("",  VaadinIcons.MENU, null);
            menuItem.setStyleName("icon-only");

            dateItem = menuItem.addItem("Date", null, null);
            addPopupItem(dateItem, "Start Date", startDatePopup);
            addPopupItem(dateItem, "End Date",   endDatePopup);

            categoryItem = menuItem.addItem("Category", null, null);
            addCategoryItem = categoryItem.addItem("Add Category", null, null);
            removeCategoryItem = categoryItem.addItem("Remove Category", null, null);

            statusItem = menuItem.addItem("Status", null, null);
            MenuBar.MenuItem saleTypeItem = statusItem.addItem("Sale Type", null, null);
            addSaleTypeItem(saleTypeItem, SaleType.Purchase);
            addSaleTypeItem(saleTypeItem, SaleType.Auction);
            addSaleTypeItem(saleTypeItem, SaleType.Drop);

            MenuBar.MenuItem saleStatusItem = statusItem.addItem("Sale Status", null, null);
            addStatusTypeItem(saleStatusItem, SaleStatus.Preview);
            addStatusTypeItem(saleStatusItem, SaleStatus.Available);
            addStatusTypeItem(saleStatusItem, SaleStatus.Sold);

            addCategoryItem.setEnabled(false);
            removeCategoryItem.setEnabled(false);
        }

        MenuBar.MenuItem addPopupItem(MenuBar.MenuItem menuItem, String caption, PopupView popup)
        {
            return menuItem.addItem(caption, null, new MenuBar.Command() {
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                        popup.setPopupVisible(true);
                }
            });
        }

        void addSaleTypeItem(MenuBar.MenuItem menuItem, SaleType saleType)
        {
            menuItem.addItem(saleType.toString(), null, new MenuBar.Command() {
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    itemsAdmin.setSelectedItems(saleType);
                }
            });
        }

        void addStatusTypeItem(MenuBar.MenuItem menuItem, SaleStatus status)
        {
            menuItem.addItem(status.toString(), null, new MenuBar.Command() {
                public void menuSelected(MenuBar.MenuItem selectedItem) { itemsAdmin.setSelectedItems(status); }
            });
        }


        public PopupView getStartDatePopup()
        {
            return startDatePopup;
        }
        public PopupView getEndDatePopup()
        {
            return endDatePopup;
        }

        public void setItemsSelected(boolean setItemsSelected)
        {
            dateItem.setEnabled(setItemsSelected);
            categoryItem.setEnabled(setItemsSelected);
            statusItem.setEnabled(setItemsSelected);
        }

        private Component createDatePopupLayout(EntityField field)
        {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setMargin(true);
            layout.setSpacing(true);
            layout.setSizeUndefined();

            DateTimeField dateField = createDateField(field.getDisplayName(), new Date());
            dateField.addValueChangeListener(event -> { itemsAdmin.setSelectedItems(field, PageUtils.getDate(dateField)); });

            layout.addComponents(dateField);
            return layout;
        }

        public  DateTimeField createDateField(String name, Date value)
        {
            DateTimeField field = new DateTimeField(name);
            field.addStyleName(ValoTheme.TEXTFIELD_TINY);
            field.addStyleName(ValoTheme.LABEL_TINY);
            field.addStyleName(ValoTheme.LABEL_NO_MARGIN);

            if (value != null) { field.setValue(PageUtils.getLocalDateTime(value)); }

            return field;
        }


        void setAddCategories(List<OrgTag> categories)
        {
            addCategoryItem.removeChildren();
            if (categories == null || categories.isEmpty())
            {
                addCategoryItem.setEnabled(false);
                return;
            }

            addCategoryItem.setEnabled(true);
            for (OrgTag category : categories)
            {
                addCategoryItem.addItem(category.getName(), null, new MenuBar.Command() {
                    public void menuSelected(MenuBar.MenuItem selectedItem)
                    {
                        itemsAdmin.addCategoryToSelectedItems(category);
                    }
                });
            }
        }

        void setRemoveCategories(List<OrgTag> categories)
        {
            removeCategoryItem.removeChildren();
            if (categories == null || categories.isEmpty())
            {
                removeCategoryItem.setEnabled(false);
                return;
            }

            removeCategoryItem.setEnabled(true);
            for (OrgTag category : categories)
            {
                removeCategoryItem.addItem(category.getName(), null, new MenuBar.Command() {
                    public void menuSelected(MenuBar.MenuItem selectedItem)
                    {
                        itemsAdmin.removeCategoryFromSelectedItems(category);
                    }
                });
            }
        }
    }

}
