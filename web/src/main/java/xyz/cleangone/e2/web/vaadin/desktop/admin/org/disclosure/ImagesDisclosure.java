package xyz.cleangone.e2.web.vaadin.desktop.admin.org.disclosure;

import xyz.cleangone.e2.web.vaadin.desktop.admin.org.ImageAdmin;


public class ImagesDisclosure extends BaseDisclosure
{
    ImageAdmin imageAdmin;

    public ImagesDisclosure(ImageAdmin imageAdmin)
    {
        super("Images", imageAdmin.getMainLayout());
        this.imageAdmin = imageAdmin;
        imageAdmin.setImagesDisclosure(this); // hack to dynamically update caption

        setDisclosureCaption();
    }

    public void setDisclosureCaption()
    {
        int numImages = imageAdmin.getNumImages();

        setDisclosureCaption(
            (numImages == 0 ? "No" : numImages) +
                (numImages == 1 ?  " Image" : " Images"));
    }
}
