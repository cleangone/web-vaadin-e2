package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import org.vaadin.viritin.fields.IntegerField;
import xyz.cleangone.data.aws.dynamo.dao.DynamoBaseDao;
import xyz.cleangone.data.aws.dynamo.dao.OrgDao;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.disclosure.BaseOrgDisclosure;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.disclosure.ImagesDisclosure;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.ArrayList;
import java.util.List;

import static xyz.cleangone.data.aws.dynamo.entity.organization.Organization.EVENT_CAPTION_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.organization.Organization.EVENT_CAPTION_PLURAL_FIELD;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createIntegerField;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createTextField;

public class OrgAdmin extends BaseOrgAdmin
{
    private final FormLayout formLayout = new FormLayout();

    private OrgManager orgMgr;

    public OrgAdmin(MessageDisplayer msgDisplayer)
    {
        super(msgDisplayer);

        setSizeFull();
        setMargin(true);
        setSpacing(true);

        formLayout.setWidth("100%");
        formLayout.setSizeUndefined();
        formLayout.setMargin(false);
        formLayout.setSpacing(false);
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
        Organization org = orgMgr.getOrg();
        OrgDao dao = orgMgr.getOrgDao();

        formLayout.removeAllComponents();

        formLayout.addComponent(new NameDisclosure(org, dao));
        formLayout.addComponent(new BannerDisclosure(org, orgMgr, dao));
        formLayout.addComponent(new BannerTextDisclosure(org, dao));
        formLayout.addComponent(new MenuDisclosure(org, dao));
        formLayout.addComponent(new LayoutDisclosure(org, dao));
        formLayout.addComponent(new ImagesDisclosure(imageAdmin));
        formLayout.addComponent(new IntroHtmlDisclosure(org, dao));
        formLayout.addComponent(new EventDisclosure(org, dao));
        formLayout.addComponent(new PaymentProcessorDisclosure(org, dao));
    }

    class LayoutDisclosure extends BaseOrgDisclosure
    {
        LayoutDisclosure(Organization org, OrgDao dao)
        {
            super("Layout", new FormLayout(), org);

            setDisclosureCaption();
            mainLayout.addComponents(
                createIntegerField(Organization.LEFT_WIDTH_FIELD, dao),
                createIntegerField(Organization.CENTER_WIDTH_FIELD, dao),
                createIntegerField(Organization.RIGHT_WIDTH_FIELD, dao));
        }

        public Component createIntegerField(EntityField field, OrgDao dao)
        {
            IntegerField intField = VaadinUtils.createIntegerField(field, org, dao, 5, msgDisplayer);
            intField.addValueChangeListener(event -> setDisclosureCaption());
            return intField;
        }

        public void setDisclosureCaption()
        {
            String msg = (org.getLeftColWidth() == 0 && org.getCenterColWidth() == 0 && org.getRightColWidth() == 0) ?
                "Not set" :
                "Left: "  + org.getLeftColWidth() +
                    (org.getCenterColWidth() == 0 ? "" : ", Center: " + org.getCenterColWidth()) +
                    (org.getRightColWidth() == 0  ? "" : ", Right: "  + org.getRightColWidth()) +
                    " pixels";
            setDisclosureCaption(msg);
        }
    }

    class PaymentProcessorDisclosure extends BaseOrgDisclosure
    {
        private OrgDao dao;
        private List<TextField> textFields = new ArrayList<>();

        PaymentProcessorDisclosure(Organization org, OrgDao dao)
        {
            super("Payment Processor", new FormLayout(), org);;
            this.dao = dao;

            RadioButtonGroup<Organization.PaymentProcessorType> paymentProcessors = new RadioButtonGroup<>("Processor Type");
            paymentProcessors.setItems(Organization.PaymentProcessorType.iATS, Organization.PaymentProcessorType.None);
            if (org.getPaymentProcessorType() != null) { paymentProcessors.setValue(org.getPaymentProcessorType()); }
            paymentProcessors.addValueChangeListener(event -> {
                org.setPaymentProcessorType((Organization.PaymentProcessorType)event.getValue());
                dao.save(org);
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

            if (org.getPaymentProcessorType() == Organization.PaymentProcessorType.iATS)
            {
                textFields.add(createTextField(Organization.IATS_AGENT_CODE_FIELD, org, dao, 15, msgDisplayer));
                textFields.add(createObscuredTextField(Organization.IATS_PASSWORD_FIELD, org, dao, 15, msgDisplayer));
            }

            textFields.forEach(textField -> mainLayout.addComponent(textField));
        }

        public void setDisclosureCaption()
        {
            setDisclosureCaption(org.getPaymentProcessorType() == null ? "Not set" :  org.getPaymentProcessorType().toString());
        }
    }

    protected class EventDisclosure extends BaseOrgDisclosure
    {
        public EventDisclosure(Organization org, DynamoBaseDao dao)
        {
            super("Event Caption", new FormLayout(), org);
            setDisclosureCaption();
            mainLayout.addComponents(
                createListeningTextField(EVENT_CAPTION_FIELD, baseOrg, dao, msgDisplayer, event -> setDisclosureCaption()),
                createListeningTextField(EVENT_CAPTION_PLURAL_FIELD, baseOrg, dao, msgDisplayer, event -> setDisclosureCaption()));
        }

        public void setDisclosureCaption()
        {
            setDisclosureCaption(org.getEventCaption() == null && org.getEventCaptionPlural() == null ?
                "Event/Events (Default)" :
                org.getEventCaption() + "/" + org.getEventCaptionPlural());
        }
    }


    public TextField createObscuredTextField(
        EntityField field, Organization entity, DynamoBaseDao dao, float widthInEm, MessageDisplayer msgDisplayer)
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
