package xyz.cleangone.e2.web.vaadin.desktop.admin.event;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.alump.ckeditor.CKEditorTextField;
import org.vaadin.viritin.components.DisclosurePanel;
import xyz.cleangone.data.aws.dynamo.dao.DynamoBaseDao;
import xyz.cleangone.data.aws.dynamo.dao.EventDao;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.org.BaseOrgAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.org.disclosure.BaseCustomComponent;
import xyz.cleangone.e2.web.vaadin.desktop.admin.org.disclosure.BaseDisclosure;
import xyz.cleangone.e2.web.vaadin.desktop.admin.org.disclosure.BaseOrgDisclosure;
import xyz.cleangone.e2.web.vaadin.desktop.admin.org.disclosure.ImagesDisclosure;
import xyz.cleangone.e2.web.vaadin.util.DisclosureUtils;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import static java.util.Objects.requireNonNull;
import static xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg.INTRO_HTML_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent.BLURB_HTML_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent.ITER_COUNT_LABEL_PLURAL_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent.ITER_LABEL_SINGULAR_FIELD;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createCkEditor;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createIntegerField;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.createTextField;

public class GeneralAdmin extends BaseOrgAdmin
{
    private final EventsAdminPage eventsAdmin;
    private EventManager eventMgr;
    private EventDao eventDao;

    private final FormLayout formLayout = new FormLayout();

    public GeneralAdmin(EventsAdminPage eventsAdmin, MessageDisplayer msgDisplayer)
    {
        super(msgDisplayer);

        this.eventsAdmin = eventsAdmin;

        setMargin(false);
        setSpacing(false);

        formLayout.setMargin(true);
        formLayout.setSpacing(false);
    }

    public void set(SessionManager sessionMgr)
    {
        eventMgr = sessionMgr.getEventManager();
        eventDao = eventMgr.getEventDao();
    }

    public void set()
    {
        imageAdmin.set(eventMgr, getUI());

        removeAllComponents();
        formLayout.removeAllComponents();

        OrgEvent event = requireNonNull(eventMgr.getEvent());

        formLayout.addComponent(new NameDisclosure(event, eventDao));
        formLayout.addComponent(new StatusDisclosure(event));
        formLayout.addComponent(new BannerDisclosure(event, eventMgr, eventDao));
        formLayout.addComponent(new BannerTextDisclosure(event, eventDao));
        formLayout.addComponent(new MenuDisclosure(event, eventDao));
        formLayout.addComponent(new ItemsDisclosure(event));
        formLayout.addComponent(new DonationsDisclosure(event));
        formLayout.addComponent(new ImagesDisclosure(imageAdmin));
        formLayout.addComponent(new IntroHtmlDisclosure(event, eventDao));
        formLayout.addComponent(new BlurbHtmlDisclosure(event, eventDao));

        addComponents(formLayout);
        setExpandRatio(formLayout, 1.0f);
    }

    private CheckBox createCheckBox(EntityField field, OrgEvent orgEvent, BaseDisclosure disclosure)
    {
        return DisclosureUtils.createCheckBox(field, orgEvent, eventDao, msgDisplayer, disclosure);
    }

    class StatusDisclosure extends BaseOrgDisclosure
    {
        StatusDisclosure(OrgEvent event)
        {
            super("Status", new FormLayout(), event);

            setDisclosureCaption();


            CheckBox useOrgBannerCheckbox = createCheckBox(OrgEvent.USE_ORG_BANNER_FIELD, event, this);
//            useOrgBannerCheckbox.addValueChangeListener(e -> setDisclosureCaption());

            mainLayout.addComponents(
                createCheckBox(OrgEvent.ENABLED_FIELD, event, this),
                useOrgBannerCheckbox,
                createCheckBox(OrgEvent.USER_CAN_REGISTER_FIELD, event, this),
                createCheckBox(OrgEvent.EVENT_COMPLETED_FIELD, event, this));
        }

        // todo - add crap to change contents of Banner disclosures based on useOrgBanner
        public void setOpen(boolean open)
        {

        }



        public void setDisclosureCaption()
        {
            String enabledTxt = orgEvent.getEnabled() ?  "Enabled" : "Disabled";
            String orgBannerTxt = orgEvent.getUseOrgBanner() ?  ", Use Org Banner" : "";
            String registerTxt = orgEvent.getUserCanRegister() ?  ", Users can register" : "";
            String completedTxt = orgEvent.getEventCompleted() ?  ", Completed" : "";

            setDisclosureCaption(enabledTxt + orgBannerTxt + registerTxt + completedTxt);
        }
    }

    class ItemsDisclosure extends BaseOrgDisclosure
    {
        ItemsDisclosure(OrgEvent event)
        {
            super("Items", new FormLayout(), event);

            setDisclosureCaption();

            mainLayout.addComponents(
                createCheckBox(OrgEvent.DISPLAY_CATEGORIES_FIELD, event, this));
        }

        public void setDisclosureCaption()
        {
            setDisclosureCaption("Categories" + (orgEvent.getDisplayCategories() ?  "" : " Not") + " Displayed" );
        }
    }

    class DonationsDisclosure extends BaseOrgDisclosure
    {
        DonationsDisclosure(OrgEvent event)
        {
            super("Donations", new FormLayout(), event);

            setDisclosureCaption();

            mainLayout.addComponents(
                createCheckBox(OrgEvent.ACCEPT_DONATIONS_FIELD, event, this),
                createCheckBox(OrgEvent.ACCEPT_PLEDGES_FIELD, event, this),
                new IterationComponent(event));
        }

        public void setDisclosureCaption()
        {
            String caption = "Donations/Pledges not accepted";
            if (orgEvent.getAcceptDonations() && orgEvent.getAcceptPledges()) { caption = "Donations, Pledges accepted"; }
            else if (orgEvent.getAcceptDonations()) { caption = "Donations accepted"; }
            else if (orgEvent.getAcceptPledges()) { caption = "Pledges accepted"; }

            setDisclosureCaption(caption);
        }

        class IterationComponent extends BaseCustomComponent
        {
            IterationComponent(OrgEvent event)
            {
                super(null, new HorizontalLayout());

                mainLayout.addComponents(
                    createTextField(ITER_LABEL_SINGULAR_FIELD, event, eventDao, 12, msgDisplayer),
                    createTextField(ITER_COUNT_LABEL_PLURAL_FIELD, event, eventDao, 12, msgDisplayer),
                    createIntegerField(OrgEvent.ESTIMATED_ITERATIONS_FIELD, event, eventDao, 5, msgDisplayer));

                setCompositionRoot(mainLayout);
            }
        }
    }

    protected class BlurbHtmlDisclosure extends BaseOrgDisclosure
    {
        public BlurbHtmlDisclosure(OrgEvent event, DynamoBaseDao dao)
        {
            super("Blurb HTML", new HorizontalLayout(), event);

            setDisclosureCaption();

            CKEditorTextField editorField = createCkEditor(BLURB_HTML_FIELD, event, dao, msgDisplayer);
            editorField.addValueChangeListener(e -> setDisclosureCaption());

            mainLayout.addComponents(editorField);
        }

        public void setDisclosureCaption()
        {
            setDisclosureCaption("Blurb HTML " + (orgEvent.getBlurbHtml() == null ? " not" : "") + " set");
        }
    }

}