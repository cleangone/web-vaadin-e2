package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.vaadin.event.LayoutEvents;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import xyz.cleangone.data.manager.ImageContainerManager;
import xyz.cleangone.data.manager.ImageManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.disclosure.ImagesDisclosure;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageDisplayer;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageLabel;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageUploader;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import java.util.List;

public class ImageAdmin implements ImageDisplayer
{
    private final MessageDisplayer msgDisplayer;

    private ImageContainerManager icMgr;
    private UI ui;
    private ImageManager imageMgr;
    private ImagesDisclosure imagesDisclosure; // bit of a hack

    private VerticalLayout mainLayout = new VerticalLayout();
    private HorizontalLayout imagesLayout = new HorizontalLayout();

    public ImageAdmin(MessageDisplayer msgDisplayer)
    {
        this.msgDisplayer = msgDisplayer;

        mainLayout.setMargin(false);
        mainLayout.setSpacing(true);
    }

    public void set(ImageContainerManager icMgr, UI ui)
    {
        this.icMgr = icMgr;
        this.ui = ui;
        imageMgr = icMgr.getImageManager();

        mainLayout.removeAllComponents();
        mainLayout.addComponents(imagesLayout, createImageUpload());

        setImages();
    }

    public VerticalLayout getMainLayout()
    {
        return mainLayout;
    }

    public int getNumImages()
    {
        List<S3Link> images = icMgr.getImages();
        return images == null ? 0 : images.size();
    }

    public void setImages()
    {
        imagesLayout.removeAllComponents();

        List<S3Link> images = icMgr.getImages();
        if (images == null) { return; }

        String primaryUrl = icMgr.getPrimaryUrl();

        for (S3Link image : images)
        {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setMargin(false);
            layout.setSpacing(false);

            VerticalLayout popupLayout = new VerticalLayout();
            popupLayout.setMargin(false);
            popupLayout.setSpacing(false);
            popupLayout.setSizeUndefined();
            PopupView popup = new PopupView(null, popupLayout);

            String imageUrl = imageMgr.getUrl(image);
            ImageLabel imageLabel = new ImageLabel(imageUrl).withHref();
            layout.addComponent(imageLabel);

            // can delete images other than the primary (used for banner, etc)
            if (!imageUrl.equals(primaryUrl))
            {
                popupLayout.addComponent(buildDeleteButton(image));
                layout.addLayoutClickListener(new LayoutEvents.LayoutClickListener()
                {
                    public void layoutClick(LayoutEvents.LayoutClickEvent event)
                    {
                        if (event.getButton() == MouseEventDetails.MouseButton.RIGHT)
                        {
                            popup.setPopupVisible(true);
                        }
                    }
                });

                layout.addComponent(popup);
                layout.setComponentAlignment(popup, new Alignment(AlignmentInfo.Bits.ALIGNMENT_VERTICAL_CENTER));
            }

            imagesLayout.addComponent(layout);
        }

        if (imagesDisclosure != null) { imagesDisclosure.setDisclosureCaption(); }
    }

    private Button buildDeleteButton(S3Link image)
    {
        Button button = createDeleteButton("Delete Image");
        button.setCaption("Delete");
        button.addStyleName(ValoTheme.TEXTFIELD_TINY);
        button.addClickListener(e -> confirmDelete(image));

        return button;
    }

    private void confirmDelete(S3Link image)
    {
        ConfirmDialog.show(ui, "Confirm Image Delete", "Delete image?", "Delete", "Cancel", new ConfirmDialog.Listener() {
            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    imageMgr.deleteImage(image);
                    setImages();
                }
            }
        });
    }

    private Component createImageUpload()
    {
        ImageUploader receiver = new ImageUploader(icMgr.getImageManager(), this, msgDisplayer);

        Upload upload = new Upload(null, receiver);
        upload.addStyleName(ValoTheme.BUTTON_SMALL);
        upload.setButtonCaption("Upload New Image");
        upload.addSucceededListener(receiver);

        return upload;
    }

    public void setImagesDisclosure(ImagesDisclosure imagesDisclosure)
    {
        this.imagesDisclosure = imagesDisclosure;
    }
}
