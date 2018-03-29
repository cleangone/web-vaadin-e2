package xyz.cleangone.e2.web.vaadin.desktop.admin.org.disclosure;

import com.vaadin.ui.AbstractOrderedLayout;
import org.vaadin.viritin.components.DisclosurePanel;


public abstract class BaseDisclosure extends BaseCustomComponent
{
    private final MyDisclosurePanel disclosurePanel;

    public BaseDisclosure(String caption, AbstractOrderedLayout layout)
    {
        super(caption, layout);

        // provide a hook into open/close
        disclosurePanel = new MyDisclosurePanel(mainLayout, this);
        setCompositionRoot(disclosurePanel);
    }

    public void setOpen(boolean open) { }

    public abstract void setDisclosureCaption();

    public void setDisclosureCaption(String disclosureCaption)
    {
        disclosurePanel.setCaption(disclosureCaption);
    }
}
