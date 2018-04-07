package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org;

import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.ui.*;
import com.vaadin.ui.dnd.DropTargetExtension;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.alump.ckeditor.CKEditorTextField;
import xyz.cleangone.data.aws.dynamo.dao.DynamoBaseDao;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg;
import xyz.cleangone.data.manager.ImageContainerManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.disclosure.BaseOrgDisclosure;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageLabel;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;
import static xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg.*;

public abstract class BaseOrgAdmin extends BaseAdmin
{
    protected final ImageAdmin imageAdmin;

    public BaseOrgAdmin(MessageDisplayer msgDisplayer)
    {
        super(msgDisplayer);

        imageAdmin = new ImageAdmin(msgDisplayer);
    }

    protected class NameDisclosure extends BaseOrgDisclosure
    {
        public NameDisclosure(BaseOrg baseOrg, DynamoBaseDao dao)
        {
            super("Name", new FormLayout(), baseOrg);

            setDisclosureCaption();

            TextField nameField = createTextField(NAME_FIELD, baseOrg, dao, msgDisplayer);
            nameField.addValueChangeListener(event -> setDisclosureCaption());

            mainLayout.addComponent(nameField);
        }

        public void setDisclosureCaption()
        {
            setDisclosureCaption(baseOrg.getName());
        }
    }

    // custom component that holds the banner and banner background color
    protected class BannerDisclosure extends BaseOrgDisclosure
    {
        // todo - should have icMgr.getDao
        ImageContainerManager icMgr;
        DynamoBaseDao dao;

        public BannerDisclosure(BaseOrg baseOrg, ImageContainerManager icMgr, DynamoBaseDao dao)
        {
            super("Banner", new HorizontalLayout(), baseOrg);
            this.icMgr = icMgr;
            this.dao = dao;

            setDisclosure();
        }

        public void setDisclosure()
        {
            mainLayout.removeAllComponents();

            BannerImageLabel bannerImageLabel = new BannerImageLabel(baseOrg.getBannerUrl());
            addDropTargetExtension(bannerImageLabel, baseOrg, icMgr);

            setDisclosureCaption();

            mainLayout.addComponents(bannerImageLabel,
                new ColorPickerComponent(BANNER_BKGND_COLOR_FIELD, baseOrg, dao, msgDisplayer, Color.WHITE));
        }

        private void addDropTargetExtension(BannerImageLabel target, BaseOrg baseOrg, ImageContainerManager icMgr)
        {
            DropTargetExtension<BannerImageLabel> dropTarget = new DropTargetExtension<>(target);
            dropTarget.addDropListener(event -> {
                event.getDragSourceComponent().ifPresent(dragSource -> {
                    if (dragSource instanceof ImageLabel) {
                        ImageLabel imageLabel = (ImageLabel)dragSource;
                        baseOrg.setBannerUrl(imageLabel.getUrl());
                        icMgr.save();
                        setDisclosure();
                    }
                });
            });
        }

        public void setDisclosureCaption()
        {
            setDisclosureCaption("Banner " + (baseOrg.getBannerHtml() == null ? " not" : "") + " set");
        }

        class BannerImageLabel extends CustomComponent
        {
            BannerImageLabel(String url)
            {
                if (StringUtils.isBlank(url))
                {
                    Panel panel = new Panel("");
                    panel.setWidth(200, Unit.PIXELS);
                    panel.setHeight(100, Unit.PIXELS);

                    Label label = new Label("Drop Banner Image Here");
                    VerticalLayout layout = new VerticalLayout(label);
                    layout.setWidth("100%");
                    layout.setHeight("100%");
                    layout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);

                    panel.setContent(layout);
                    setCompositionRoot(panel);
                }
                else
                {
                    setCompositionRoot(new ImageLabel(url).withHref());
                }
            }
        }
    }

    protected class BannerTextDisclosure extends BaseOrgDisclosure
    {
        public BannerTextDisclosure(BaseOrg baseOrg, DynamoBaseDao dao)
        {
            super("Banner Text", new HorizontalLayout(), baseOrg);

            setDisclosureCaption();

            TextField bannerHtmlField = createTextField(BANNER_HTML_FIELD, baseOrg, dao, 20, msgDisplayer);
            bannerHtmlField.addValueChangeListener(event -> setDisclosureCaption());

            mainLayout.addComponents(
                bannerHtmlField,
                createTextField(BANNER_TEXT_SIZE_FIELD,  baseOrg, dao, 5, msgDisplayer),
                new ColorPickerComponent(BANNER_TEXT_COLOR_FIELD, baseOrg, dao, msgDisplayer, Color.WHITE),
                new ColorPickerComponent(BANNER_DROPSHADOW_COLOR_FIELD, baseOrg, dao, msgDisplayer, Color.WHITE));
        }

        public void setDisclosureCaption()
        {
            setDisclosureCaption(baseOrg.getBannerHtml());
        }
    }

    // custom component that holds the menu background and menu text colors
    protected class MenuDisclosure extends BaseOrgDisclosure
    {
        public MenuDisclosure(BaseOrg baseOrg, DynamoBaseDao dao)
        {
            super("Menu Bar/Col", new HorizontalLayout(), baseOrg);

            setDisclosureCaption();

            mainLayout.addComponents(
                new ColorPickerComponent(BAR_BKGND_COLOR_FIELD, baseOrg, dao, msgDisplayer, Color.WHITE),
                new ColorPickerComponent(NAV_BKGND_COLOR_FIELD, baseOrg, dao, msgDisplayer, Color.WHITE),
                new ColorPickerComponent(NAV_TEXT_COLOR_FIELD, baseOrg, dao, msgDisplayer, Color.BLACK),
                new ColorPickerComponent(NAV_SELECTED_TEXT_COLOR_FIELD, baseOrg, dao, msgDisplayer, Color.BLACK));
        }

        public void setDisclosureCaption()
        {
            String caption = baseOrg.getBannerBackgroundColor() == null &&
                baseOrg.getNavBackgroundColor() == null &&
                baseOrg.getNavTextColor() == null &&
                baseOrg.getNavSelectedTextColor() == null ? "not set" : "set";

            setDisclosureCaption(caption);
        }

    }

    protected class IntroHtmlDisclosure extends BaseOrgDisclosure
    {
        public IntroHtmlDisclosure(BaseOrg baseOrg, DynamoBaseDao dao)
        {
            super("Intro HTML", new HorizontalLayout(), baseOrg);

            setDisclosureCaption();

            CKEditorTextField editorField = createCkEditor(INTRO_HTML_FIELD, baseOrg, dao, msgDisplayer);
            editorField.addValueChangeListener(event -> setDisclosureCaption());

            mainLayout.addComponents(editorField);
        }

        public void setDisclosureCaption()
        {
            setDisclosureCaption("Intro HTML " + (baseOrg.getIntroHtml() == null ? " not" : "") + " set");
        }
    }

    class ColorPickerComponent extends CustomComponent
    {
        ColorPickerComponent(EntityField field, BaseEntity entity, DynamoBaseDao dao, MessageDisplayer msgDisplayer, Color defaultColor)
        {
            setCaption(field.getDisplayName());

            Color initialColor = defaultColor;
            String entityColor = entity.get(field);
            if (entityColor != null)
            {
                try
                {
                    java.awt.Color javaColor = java.awt.Color.decode(entityColor);
                    initialColor = new Color(javaColor.getRed(), javaColor.getGreen(), javaColor.getBlue());
                }
                catch (Exception e) { ; }  // bad color in field - use default
            }

            ColorPicker colorPicker = new ColorPicker(getCaption(), initialColor);
            colorPicker.setTextfieldVisibility(true);
            colorPicker.addValueChangeListener(event -> {
                entity.set(field, event.getValue().getCSS());
                dao.save(entity);
                msgDisplayer.displayMessage(field.getDisplayName() + " saved");
            });

            setCompositionRoot(colorPicker);
        }
    }

}
