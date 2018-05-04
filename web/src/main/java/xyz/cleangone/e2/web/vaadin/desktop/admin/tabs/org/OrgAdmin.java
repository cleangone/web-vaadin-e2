package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org;

import com.vaadin.data.HasValue;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import org.vaadin.viritin.fields.IntegerField;
import xyz.cleangone.data.aws.dynamo.dao.DynamoBaseDao;
import xyz.cleangone.data.aws.dynamo.dao.org.OrgDao;
import xyz.cleangone.data.aws.dynamo.dao.org.PaymentProcessorDao;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.organization.PaymentProcessor;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.disclosure.BaseOrgDisclosure;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.disclosure.ImagesDisclosure;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.ArrayList;
import java.util.List;

import static xyz.cleangone.data.aws.dynamo.entity.organization.Organization.*;
import static xyz.cleangone.data.aws.dynamo.entity.organization.PaymentProcessor.*;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class OrgAdmin extends BaseOrgAdmin
{
    private final FormLayout formLayout = formLayout(MARGIN_FALSE, SPACING_FALSE, WIDTH_100_PCT, VaadinUtils.SIZE_UNDEFINED);

    private OrgManager orgMgr;
    private OrgDao orgDao;
    private PaymentProcessorDao paymentProcessorDao;
    private Organization org;
    private PaymentProcessor paymentProcessor;

    public OrgAdmin(MessageDisplayer msgDisplayer)
    {
        super(msgDisplayer);
        setLayout(this, MARGIN_TRUE, SPACING_TRUE, SIZE_FULL);

        addComponent(formLayout);
    }

    public void set(SessionManager sessionMgr)
    {
        orgMgr = sessionMgr.getOrgManager();
        set();
    }

    public void set()
    {
        imageAdmin.set(orgMgr, getUI());

        org = orgMgr.getOrg();
        paymentProcessor = orgMgr.getPaymentProcessor();

        orgDao = orgMgr.getOrgDao();
        paymentProcessorDao = orgMgr.getPaymentProcessorDao();

        formLayout.removeAllComponents();

        formLayout.addComponent(new NameDisclosure(org, orgDao));
        formLayout.addComponent(new BannerDisclosure(org, orgMgr, orgDao));
        formLayout.addComponent(new BannerTextDisclosure(org, orgDao));
        formLayout.addComponent(new MenuDisclosure(org, orgDao));
        formLayout.addComponent(new LayoutDisclosure(org));
        formLayout.addComponent(new ImagesDisclosure(imageAdmin));
        formLayout.addComponent(new IntroHtmlDisclosure(org, orgDao));
        formLayout.addComponent(new EventDisclosure(org));
        formLayout.addComponent(new PaymentProcessorDisclosure(org));
    }

    public TextField createListeningTextField(EntityField field, BaseEntity entity, HasValue.ValueChangeListener<String> listener)
    {
        return createListeningTextField(field, entity, orgDao, msgDisplayer, listener);
    }

    class LayoutDisclosure extends BaseOrgDisclosure
    {
        LayoutDisclosure(Organization org)
        {
            super("Layout", new FormLayout(), org);

            setDisclosureCaption();
            mainLayout.addComponents(
                createIntegerField(Organization.LEFT_WIDTH_FIELD),
                createIntegerField(Organization.CENTER_WIDTH_FIELD),
                createIntegerField(Organization.RIGHT_WIDTH_FIELD),
                createIntegerField(Organization.MAX_LEFT_WIDTH_FIELD),
                createIntegerField(Organization.MAX_CENTER_WIDTH_FIELD),
                createIntegerField(Organization.MAX_RIGHT_WIDTH_FIELD));
        }

        public Component createIntegerField(EntityField field)
        {
            IntegerField intField = VaadinUtils.createIntegerField(field, org, orgDao, 5, msgDisplayer);
            intField.addValueChangeListener(event -> setDisclosureCaption());
            return intField;
        }

        public void setDisclosureCaption()
        {
            String msg = (org.getLeftColWidth() == 0 && org.getCenterColWidth() == 0 && org.getRightColWidth() == 0) ?
                "Not set" :
                "Left: "     + org.getLeftColWidth()   + (org.getMaxLeftColWidth() == 0   ? "" : "/" + org.getMaxLeftColWidth()) +
                ", Center: " + org.getCenterColWidth() + (org.getMaxCenterColWidth() == 0 ? "" : "/" + org.getMaxCenterColWidth()) +
                ",  Right: " + org.getRightColWidth()  + (org.getMaxRightColWidth() == 0  ? "" : "/" + org.getMaxRightColWidth()) +
                " pixels";
            setDisclosureCaption(msg);
        }
    }

    class PaymentProcessorDisclosure extends BaseOrgDisclosure
    {
        private List<TextField> textFields = new ArrayList<>();

        PaymentProcessorDisclosure(Organization org)
        {
            super("Payment Processor", new FormLayout(), org);

            RadioButtonGroup<PaymentProcessor.PaymentProcessorType> paymentProcessors = new RadioButtonGroup<>("Processor Type");

            paymentProcessors.setItems(PaymentProcessor.PaymentProcessorType.iATS, PaymentProcessor.PaymentProcessorType.None);

            if (paymentProcessor != null) { paymentProcessors.setValue(paymentProcessor.getType()); }
            paymentProcessors.addValueChangeListener(event -> {
                if (paymentProcessor == null) { paymentProcessor = orgMgr.createPaymentProcessor(); }
                paymentProcessor.setType((PaymentProcessor.PaymentProcessorType)event.getValue());
                paymentProcessorDao.save(paymentProcessor);
                msgDisplayer.displayMessage("Payment Processor saved");
                setDisclosureCaption();
                setTextFields();
            });

            mainLayout.addComponent(paymentProcessors);
            setDisclosureCaption();
            setTextFields();
        }

        void setTextFields()
        {
            textFields.forEach(textField -> mainLayout.removeComponent(textField));
            textFields.clear();

            if (paymentProcessor != null && paymentProcessor.isIats())
            {
                textFields.add(createTextField(IATS_AGENT_CODE_FIELD, paymentProcessor, paymentProcessorDao, 15, msgDisplayer));
                textFields.add(createObscuredTextField(IATS_PASSWORD_FIELD, paymentProcessor, paymentProcessorDao, 15, msgDisplayer));
            }

            textFields.forEach(textField -> mainLayout.addComponent(textField));
        }

        public void setDisclosureCaption()
        {
            setDisclosureCaption(paymentProcessor == null ? "Not set" :  paymentProcessor.getType().toString());
        }
    }

    protected class EventDisclosure extends BaseOrgDisclosure
    {
        public EventDisclosure(Organization org)
        {
            super("Event Caption", new FormLayout(), org);
            setDisclosureCaption();
            mainLayout.addComponents(
                createListeningTextField(EVENT_CAPTION_FIELD, baseOrg, event -> setDisclosureCaption()),
                createListeningTextField(EVENT_CAPTION_PLURAL_FIELD, baseOrg, event -> setDisclosureCaption()));
        }

        public void setDisclosureCaption()
        {
            setDisclosureCaption(org.getEventCaption() == null && org.getEventCaptionPlural() == null ?
                "Event/Events (Default)" :
                org.getEventCaption() + "/" + org.getEventCaptionPlural());
        }
    }


    public TextField createObscuredTextField(
        EntityField field, BaseEntity entity, DynamoBaseDao dao, float widthInEm, MessageDisplayer msgDisplayer)
    {
        String OBSCURED = "*****";

        TextField textField = createTextField(field.getDisplayName());
        if (entity.get(field) != null) { textField.setValue(OBSCURED); }

        textField.setWidth(widthInEm, Sizeable.Unit.EM);
        textField.addValueChangeListener(event -> {
            String value = (String)event.getValue();

            if (value.isEmpty())
            {
                Notification.show("Password not set", Notification.Type.ERROR_MESSAGE);
            }
            else if (!OBSCURED.equals(value))
            {
                entity.set(field, value);
                dao.save(entity);

                msgDisplayer.displayMessage(field.getDisplayName() + " saved");
                textField.setValue(OBSCURED);
            }
       });

        return textField;
    }
}
