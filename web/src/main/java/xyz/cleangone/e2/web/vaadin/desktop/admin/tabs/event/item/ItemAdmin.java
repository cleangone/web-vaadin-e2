package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.item;

import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.viritin.fields.IntegerField;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.item.PurchaseItem;
import xyz.cleangone.data.aws.dynamo.entity.item.SaleStatus;
import xyz.cleangone.data.aws.dynamo.entity.item.SaleType;
import xyz.cleangone.data.manager.event.ItemManager;
import xyz.cleangone.e2.web.manager.notification.NotificationWatcher;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.ImageAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.disclosure.ImagesDisclosure;
import xyz.cleangone.web.vaadin.ui.DollarField;
import xyz.cleangone.web.vaadin.ui.MessageDisplayer;
import xyz.cleangone.web.vaadin.util.PageUtils;
import xyz.cleangone.web.vaadin.util.VaadinUtils;

import java.math.BigDecimal;
import java.util.Date;

import static xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem.*;
import static xyz.cleangone.web.vaadin.util.VaadinUtils.*;

public class ItemAdmin extends VerticalLayout
{
    private final MessageDisplayer msgDisplayer;
    private final ItemsAdmin itemsAdmin;

    private final CatalogItem item;
    private final ItemManager itemMgr;
    private final ImageAdmin imageAdmin;

    private final FormLayout formLayout = new FormLayout();

    public ItemAdmin(ItemManager itemMgr, MessageDisplayer msgDisplayer, ItemsAdmin itemsAdmin, UI ui)
    {
        this.itemMgr = itemMgr;
        this.msgDisplayer = msgDisplayer;
        this.itemsAdmin = itemsAdmin;

        item = itemMgr.getItem();
        imageAdmin = new ImageAdmin(msgDisplayer);
        imageAdmin.set(itemMgr, ui);

        setLayout(this, MARGIN_FALSE, SPACING_FALSE, BACK_PINK);

        formLayout.setMargin(false);
        formLayout.setSpacing(false);
        formLayout.setStyleName("marginLeft");

        set();
    }

    protected void set()
    {
        removeAllComponents();

        Button closeButton = VaadinUtils.createCloseButton("Close Item");
        closeButton.addClickListener(e -> itemsAdmin.setContent());

        // quantity only applicable to purchases
        IntegerField quantityField = createIntegerField(QUANTITY_FIELD);
        quantityField.setEnabled(item.isPurchase());

        IntegerField dropWindowField = createIntegerField(DROP_WINDOW_FIELD);
        dropWindowField.setEnabled(item.isDrop());

        formLayout.removeAllComponents();
        formLayout.addComponent(createTextField(NAME_FIELD));
        formLayout.addComponent(createCheckBox(PurchaseItem.ENABLED_FIELD));
        formLayout.addComponent(createDollarField(PRICE_FIELD));
        formLayout.addComponent(quantityField);
        formLayout.addComponent(dropWindowField);
        formLayout.addComponent(createIntegerField(RELATIVE_WIDTH_FIELD));
        formLayout.addComponent(createSaleTypeRadioButtonGroup(quantityField, dropWindowField));
        formLayout.addComponent(createSaleStatusRadioButtonGroup());
        formLayout.addComponent(createDateField(AVAIL_START_FIELD));
        formLayout.addComponent(createDateField(AVAIL_END_FIELD));

        formLayout.addComponent(new ImagesDisclosure(imageAdmin));

        addComponents(closeButton, formLayout);
        setExpandRatio(formLayout, 1.0f);
    }

    public TextField createTextField(EntityField field)
    {
        TextField textField = VaadinUtils.createTextField(field.getDisplayName(), item.get(field), null);
        textField.addValueChangeListener(event -> {
            item.set(field, (String)event.getValue());
            save(field);
        });

        return textField;
    }

    private IntegerField createIntegerField(EntityField field)
    {
        IntegerField intField = VaadinUtils.createIntegerField(field.getDisplayName());
        intField.setWidth(5, Unit.EM);
        intField.setValue(item.getInteger(field));
        intField.addStyleName(ValoTheme.TEXTFIELD_TINY);  // todo - doesn't look like it affects display
        intField.addStyleName(ValoTheme.LABEL_TINY);
        intField.addValueChangeListener(event -> {
            item.setInteger(field, event.getValue());
            save(field);
        });

        return intField;
    }

    private DollarField createDollarField(EntityField field)
    {
        DollarField dollarField = createDollarField(field.getDisplayName(), item.getBigDecimal(field));
        dollarField.addValueChangeListener(event -> {
            item.setBigDecimal(field, dollarField.getDollarValue());
            save(field);
        });

        return dollarField;
    }

    private DateTimeField createDateField(EntityField field)
    {
        DateTimeField dateField = createDateField(field.getDisplayName(), item.getDate(field));
        dateField.addValueChangeListener(event -> {
            item.setDate(field, PageUtils.getDate(dateField));
            save(field);
        });

        return dateField;
    }

    public static DollarField createDollarField(String name, BigDecimal value)
    {
        DollarField field = new DollarField(name);
        field.setValueChangeMode(ValueChangeMode.BLUR);
        field.addStyleName(ValoTheme.TEXTFIELD_TINY);
        field.addStyleName(ValoTheme.LABEL_TINY);
        field.addStyleName(ValoTheme.LABEL_NO_MARGIN);

        if (value != null) { field.setValue(value.toString()); } // todo refactor

        return field;
    }

    public static DateTimeField createDateField(String name, Date value)
    {
        DateTimeField field = new DateTimeField(name);
        field.addStyleName(ValoTheme.TEXTFIELD_TINY);
        field.addStyleName(ValoTheme.LABEL_TINY);
        field.addStyleName(ValoTheme.LABEL_NO_MARGIN);

        if (value != null) { field.setValue(PageUtils.getLocalDateTime(value)); }

        return field;
    }

    private RadioButtonGroup createSaleTypeRadioButtonGroup(IntegerField quantityField, IntegerField dropWindowField)
    {
        RadioButtonGroup<SaleType> group = new RadioButtonGroup<>("Sale Type");
        group.setItems(SaleType.Purchase, SaleType.Auction, SaleType.Drop);
        group.setItemCaptionGenerator(SaleType::toString);
        if (item.getSaleType() != null) { group.setValue(item.getSaleType()); }
        group.addValueChangeListener(event -> {
            item.setSaleType(event.getValue());
            if (item.isAuction() || item.isDrop()) { item.setQuantity(1); }
            quantityField.setEnabled(item.isPurchase());
            dropWindowField.setEnabled(item.isDrop());
            save(SALE_TYPE_FIELD);
        });

        return group;
    }

    private RadioButtonGroup createSaleStatusRadioButtonGroup()
    {
        RadioButtonGroup<SaleStatus> group = new RadioButtonGroup<>("Sale Status");
        group.setItems(SaleStatus.Preview, SaleStatus.Available, SaleStatus.Sold);
        group.setItemCaptionGenerator(SaleStatus::toString);
        if (item.getSaleStatus() != null) { group.setValue(item.getSaleStatus()); }
        group.addValueChangeListener(event -> {
            item.setSaleStatus(event.getValue());
            save(SALE_STATUS_FIELD);
        });

        return group;
    }



    private CheckBox createCheckBox(EntityField field)
    {
        CheckBox checkBox = VaadinUtils.createCheckBox(field.getDisplayName(), item.getBoolean(field));
        checkBox.addValueChangeListener(event -> {
            item.setBoolean(field, event.getValue());
            save(field);
        });

        return checkBox;
    }

    private void save(EntityField field)
    {
        itemMgr.save();
        msgDisplayer.displayMessage(field.getDisplayName() + " saved");

        NotificationWatcher.startWatch(item.getOrgId());
    }
}