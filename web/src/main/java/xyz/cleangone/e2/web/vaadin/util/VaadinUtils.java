package xyz.cleangone.e2.web.vaadin.util;


import com.vaadin.data.HasValue;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.alump.ckeditor.CKEditorConfig;
import org.vaadin.alump.ckeditor.CKEditorTextField;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.viritin.fields.IntegerField;
import xyz.cleangone.data.aws.dynamo.dao.DynamoBaseDao;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseMixinEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;


public class VaadinUtils
{
    public static final String IMAGE_HAND_STYLE = "with-hand";


    public static TextField createNoCaptionTextField(
        EntityField field, BaseEntity entity, DynamoBaseDao dao, String placeholder, float width, MessageDisplayer msgDisplayer)
    {
        TextField textField = createNoCaptionTextField(field, entity, dao, placeholder, msgDisplayer);
        textField.setWidth(width, Sizeable.Unit.EM);
        return textField;
    }

    public static TextField createNoCaptionTextField(
        EntityField field, BaseEntity entity, DynamoBaseDao dao, String placeholder, MessageDisplayer msgDisplayer)
    {
        TextField textField = VaadinUtils.createTextField(field, entity, dao, msgDisplayer);
        textField.setCaption(null);
        textField.setPlaceholder(placeholder);
        return textField;
    }

    public static TextField createTextField(
        EntityField field, BaseEntity entity, DynamoBaseDao dao, float widthInEm, MessageDisplayer msgDisplayer)
    {
        TextField textField = createTextField(field, entity, dao, msgDisplayer);
        textField.setWidth(widthInEm, Sizeable.Unit.EM);
        return textField;
    }

    public static TextField createTextField(
        EntityField field, BaseEntity entity, DynamoBaseDao dao, MessageDisplayer msgDisplayer)
    {
        return createTextField(field, entity, dao, msgDisplayer, null);
    }

    public static TextField createTextField(
        EntityField field, BaseEntity entity, DynamoBaseDao dao, MessageDisplayer msgDisplayer, String inputPrompt)
    {
        TextField textField = createTextField(field.getDisplayName(), entity.get(field), inputPrompt);
        textField.addValueChangeListener(event -> {
            entity.set(field, (String)event.getValue());
            dao.save(entity);

            msgDisplayer.displayMessage(field.getDisplayName() + " saved");
        });

        return textField;
    }

    public static TextField createTextField(String name, String value)
    {
        return createTextField(name, value, null);
    }

    public static TextField createTextField(String name, String value, String inputPrompt)
    {
        TextField textField = createTextField(name);

        if (value != null) { textField.setValue(value); }

//        if (inputPrompt != null)
//        {
//            textField.setInputPrompt(inputPrompt);
//        }

        return textField;
    }

    public static TextField createTextField(String name)
    {
        TextField textField = new TextField(name);
        textField.setValueChangeMode(ValueChangeMode.BLUR);
        textField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        textField.addStyleName(ValoTheme.LABEL_TINY);
        textField.addStyleName(ValoTheme.LABEL_NO_MARGIN);

        return textField;
    }


    public static TextField createGridTextField(String placeholder)
    {
        TextField textField = new TextField();
        textField.setWidth("100%");
        textField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        textField.setPlaceholder(placeholder);

        return textField;
    }

    public static IntegerField createIntegerField(
        EntityField field, BaseEntity entity, DynamoBaseDao dao, float widthInEm, MessageDisplayer msgDisplayer)
    {
        IntegerField integerField = createIntegerField(field, entity, dao, msgDisplayer);
        integerField.setWidth(widthInEm, Sizeable.Unit.EM);
        return integerField;
    }

    public static IntegerField createIntegerField(
        EntityField field, BaseEntity entity, DynamoBaseDao dao, MessageDisplayer msgDisplayer)
    {
        IntegerField intField = createIntegerField(field.getDisplayName());
        intField.setValue(entity.getInt(field));
        intField.addStyleName(ValoTheme.TEXTFIELD_TINY);  // todo - doesn't look like it affects display
        intField.addStyleName(ValoTheme.LABEL_TINY);
        intField.addValueChangeListener(event -> {
            entity.setInt(field, event.getValue());
            dao.save(entity);
            msgDisplayer.displayMessage(field.getDisplayName() + " saved");
        });

        return intField;
    }


    public static IntegerField createIntegerField(String caption)
    {
        IntegerField field = new IntegerField(caption);
        field.setWidth("100%");
        field.addStyleName(ValoTheme.TEXTFIELD_TINY);

        return field;
    }


    public static CheckBox createCheckBox(EntityField field, BaseEntity entity, DynamoBaseDao dao, MessageDisplayer msgDisplayer)
    {
        CheckBox checkBox = createCheckBox(field.getDisplayName(), entity.getBoolean(field));
        checkBox.addValueChangeListener(event -> {
            entity.setBoolean(field, event.getValue());
            dao.save(entity);
            msgDisplayer.displayMessage(field.getDisplayName() + " saved");
        });

        return checkBox;
    }

    public static CheckBox createCheckBox(String name, boolean value)
    {
        CheckBox checkBox = new CheckBox(name);
        checkBox.addStyleName(ValoTheme.TEXTFIELD_TINY);
        checkBox.addStyleName(ValoTheme.LABEL_TINY);
        checkBox.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        checkBox.setValue(value);

        return checkBox;
    }


    public static CKEditorTextField createCkEditor(
        EntityField field, BaseMixinEntity entity, DynamoBaseDao dao, MessageDisplayer messageDisplayer)
    {
        CKEditorConfig config = new CKEditorConfig();
        config.useCompactTags();
        config.disableElementsPath();
        config.setResizeDir(CKEditorConfig.RESIZE_DIR.BOTH);
        //config.disableSpellChecker();
        config.setWidth("100%");
        config.setHeight("100%");

        CKEditorTextField ckEditorTextField = new CKEditorTextField(config);
        //ckEditorTextField.setCaption(field.getDisplayName());
        ckEditorTextField.setSizeFull();

        String value = entity.get(field);
        if (value != null) { ckEditorTextField.setValue(value); }

        ckEditorTextField.addValueChangeListener(event -> {
            String updatedValue = (String)event.getValue();
            updatedValue = updatedValue.replaceAll("\n", "");
            updatedValue = updatedValue.replaceAll("\t", "");

            entity.set(field, updatedValue);
            dao.save(entity);
            messageDisplayer.displayMessage(field.getDisplayName() + " saved");
        });

        return ckEditorTextField;
    }

    public static void addFilterField(EntityField entityField, MultiFieldFilter filter, HeaderRow filterHeader)
    {
        TextField filterField = createGridTextField("Filter");
        filterField.addValueChangeListener(event ->
            filter.resetFilters(entityField, event.getValue()));

        filterHeader.getCell(entityField.getName()).setComponent(filterField);
    }

    public static Button createDeleteButton(String confirmMsg, UI ui, ConfirmDialog.Listener listener)
    {
        Button button = createDeleteButton(confirmMsg);
        button.addClickListener(e -> {
            ConfirmDialog.show(ui, "Confirm Delete", confirmMsg, "Delete", "Cancel", listener);
        });

        return button;
    }

    public static Button createCloseButton(String description) { return createIconButton(VaadinIcons.CLOSE_CIRCLE, description); }
    public static Button createDeleteButton(String description) { return createSmallIconButton(VaadinIcons.TRASH, description); }

    public static Button createDeleteButton(String description, Button.ClickListener listener)
    {
        Button button = createDeleteButton(description);
        button.addClickListener(listener);
        return button;
    }

    public static Button createSmallIconButton(VaadinIcons icon, String description)
    {
        Button button = createIconButton(icon, description);
        button.addStyleName(ValoTheme.BUTTON_SMALL);
        return button;
    }

    public static Button createIconButton(VaadinIcons icon, String description)
    {
        Button button = new Button(icon);
        button.setDescription(description);
        button.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        return button;
    }

    public static Button createTextButton(String caption, Button.ClickListener listener)
    {
        Button button = new Button(caption, listener);
        button.addStyleName(ValoTheme.TEXTFIELD_TINY);
        return button;
    }

    public static Button createTextButton(String caption)
    {
        Button button = new Button(caption);
        button.addStyleName(ValoTheme.TEXTFIELD_TINY);
        return button;
    }

    public static Button createLinkButton(String caption, Button.ClickListener listener)
    {
        Button button = new Button(caption, listener);
        button.addStyleName(ValoTheme.BUTTON_LINK);
        return button;
    }

    public static Button createLinkButton(String caption)
    {
        Button button = new Button(caption);
        button.addStyleName(ValoTheme.BUTTON_LINK);
        return button;
    }

    public static Label getHtmlLabel(String value)
    {
        String labalValue = value == null ? "" : value;
        return new Label(labalValue, ContentMode.HTML);
    }

    public static String quote(String s)
    {
        return "\"" + s + "\"";
    }


    // add Enter key shortcut to button when field in focus
    public static void addEnterKeyShortcut(Button button, TextField textField)
    {
        textField.addFocusListener(new FieldEvents.FocusListener() {
            @Override
            public void focus(FieldEvents.FocusEvent event) {
                button.setClickShortcut(ShortcutAction.KeyCode.ENTER);
            }
        });

        textField.addBlurListener(new FieldEvents.BlurListener() {
            @Override
            public void blur(FieldEvents.BlurEvent event) {
                button.removeClickShortcut();
            }
        });
    }

    public static void showError(String msg) { Notification.show(msg, Notification.Type.ERROR_MESSAGE); }

    public static String getOrDefault(String value, String defaultValue)
    {
        return value == null ? defaultValue : value;
    }
}
