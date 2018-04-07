package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event;

import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.viritin.fields.IntegerField;
import xyz.cleangone.data.aws.dynamo.dao.CatalogItemDao;
import xyz.cleangone.data.aws.dynamo.dao.DynamoBaseDao;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.manager.event.ItemManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.ImageAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.disclosure.ImagesDisclosure;
import xyz.cleangone.e2.web.vaadin.util.DollarField;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.math.BigDecimal;

import static xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem.*;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createTextField;

public class ItemAdmin extends VerticalLayout
{
    private final MessageDisplayer msgDisplayer;
    private final ItemsAdmin itemsAdmin;

    private final CatalogItem item;
    private final CatalogItemDao itemDao;
    private final ImageAdmin imageAdmin;

    private final FormLayout formLayout = new FormLayout();

    public ItemAdmin(ItemManager itemMgr, MessageDisplayer msgDisplayer, ItemsAdmin itemsAdmin, UI ui)
    {
        this.msgDisplayer = msgDisplayer;
        this.itemsAdmin = itemsAdmin;

        item = itemMgr.getItem();
        itemDao = itemMgr.getDao();
        imageAdmin = new ImageAdmin(msgDisplayer);
        imageAdmin.set(itemMgr, ui);

        setMargin(false);
        setSpacing(false);

        formLayout.setMargin(false);
        formLayout.setSpacing(false);

        set();
    }

    protected void set()
    {

        removeAllComponents();

        Button closeButton = VaadinUtils.createCloseButton("Close Item");
        closeButton.addClickListener(e -> itemsAdmin.setContent());

        formLayout.removeAllComponents();

        formLayout.addComponent(createTextField(NAME_FIELD, item, itemDao, msgDisplayer));
        formLayout.addComponent(createCheckBox(OrgEvent.ENABLED_FIELD));
        formLayout.addComponent(createDollarField(PRICE_FIELD, item, itemDao, msgDisplayer));
        formLayout.addComponent(createIntegerField(QUANTITY_FIELD, item, itemDao, 5, msgDisplayer));
        formLayout.addComponent(new ImagesDisclosure(imageAdmin));

        addComponents(closeButton, formLayout);
        setExpandRatio(formLayout, 1.0f);
    }

    public IntegerField createIntegerField(
        EntityField field, BaseEntity entity, DynamoBaseDao dao, float widthInEm, MessageDisplayer msgDisplayer)
    {
        IntegerField integerField = createIntegerField(field, entity, dao, msgDisplayer);
        integerField.setWidth(widthInEm, Unit.EM);
        return integerField;
    }

    IntegerField createIntegerField(
        EntityField field, BaseEntity entity, DynamoBaseDao dao, MessageDisplayer msgDisplayer)
    {
        IntegerField intField = VaadinUtils.createIntegerField(field.getDisplayName());
        intField.setValue(entity.getInteger(field));
        intField.addStyleName(ValoTheme.TEXTFIELD_TINY);  // todo - doesn't look like it affects display
        intField.addStyleName(ValoTheme.LABEL_TINY);
        intField.addValueChangeListener(event -> {
            entity.setInteger(field, event.getValue());
            dao.save(entity);
            msgDisplayer.displayMessage(field.getDisplayName() + " saved");
        });

        return intField;
    }

    public static DollarField createDollarField(
        EntityField field, BaseEntity entity, DynamoBaseDao dao, MessageDisplayer msgDisplayer)
    {
        DollarField dollarField = createDollarField(field.getDisplayName(), entity.getBigDecimal(field));
        dollarField.addValueChangeListener(event -> {
            entity.setBigDecimal(field, dollarField.getDollarValue());
            dao.save(entity);
            msgDisplayer.displayMessage(field.getDisplayName() + " saved");
        });

        return dollarField;
    }

    public static DollarField createDollarField(String name, BigDecimal value)
    {
        DollarField dollarField = new DollarField(name);
        dollarField.setValueChangeMode(ValueChangeMode.BLUR);
        dollarField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        dollarField.addStyleName(ValoTheme.LABEL_TINY);
        dollarField.addStyleName(ValoTheme.LABEL_NO_MARGIN);

        if (value != null) { dollarField.setValue(value.toString()); } // todo refactor

        return dollarField;
    }

    private CheckBox createCheckBox(EntityField field)
    {
        CheckBox checkBox = createCheckBox(field.getDisplayName(), item.getBoolean(field));
        checkBox.addValueChangeListener(event -> {
            item.setBoolean(field, event.getValue());
            itemDao.save(item);
            msgDisplayer.displayMessage(field.getDisplayName() + " saved");
        });

        return checkBox;
    }

    private CheckBox createCheckBox(String name, boolean value)
    {
        CheckBox checkBox = new CheckBox(name);
        checkBox.addStyleName(ValoTheme.TEXTFIELD_TINY);
        checkBox.addStyleName(ValoTheme.LABEL_TINY);
        checkBox.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        checkBox.setValue(value);

        return checkBox;
    }



//    class StatusDisclosure extends BaseOrgDisclosure
//    {
//        StatusDisclosure(OrgEvent event)
//        {
//            super("Status", new FormLayout(), event);
//
//            setDisclosureCaption();
//
//            mainLayout.addComponents(
//                createCheckBox(OrgEvent.ENABLED_FIELD, event, this),
//                createCheckBox(OrgEvent.USER_CAN_REGISTER_FIELD, event, this),
//                createCheckBox(OrgEvent.EVENT_COMPLETED_FIELD, event, this));
//        }
//
//        public void setDisclosureCaption()
//        {
//            OrgEvent event = (OrgEvent)baseOrg; // hack...
//
//            String enabledTxt = event.getEnabled() ?  "Enabled" : "Disabled";
//            String registerTxt = event.getUserCanRegister() ?  ", Users can register" : "";
//            String completedTxt = event.getEventCompleted() ?  ", Completed" : "";
//
//            setDisclosureCaption(enabledTxt + registerTxt + completedTxt);
//        }
//    }


}