package xyz.cleangone.e2.web.vaadin.util;

import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.dao.DynamoBaseDao;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.e2.web.vaadin.desktop.admin.org.disclosure.BaseDisclosure;

public class DisclosureUtils
{
    public static TextField createTextField(EntityField field, BaseEntity entity, DynamoBaseDao dao, MessageDisplayer msgDisplayer, BaseDisclosure disclosure)
    {
        TextField textField = VaadinUtils.createTextField(field, entity, dao, msgDisplayer);
        textField.addValueChangeListener(event -> disclosure.setDisclosureCaption());

        return textField;
    }

    public static CheckBox createCheckBox(EntityField field, BaseEntity entity, DynamoBaseDao dao, MessageDisplayer msgDisplayer, BaseDisclosure disclosure)
    {
        CheckBox checkBox = VaadinUtils.createCheckBox(field, entity, dao, msgDisplayer);
        checkBox.addValueChangeListener(event -> disclosure.setDisclosureCaption());
        return checkBox;
    }
}
