package xyz.cleangone.e2.web.vaadin.util.disclosure;

import com.vaadin.ui.AbstractOrderedLayout;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.disclosure.BaseCustomComponent;


public abstract class BaseDisclosure extends BaseCustomComponent
{
    private final ControllableDisclosurePanel disclosurePanel;

    public BaseDisclosure(String caption, AbstractOrderedLayout layout)
    {
        super(caption, layout);

        // provide a hook into open/close
        disclosurePanel = new ControllableDisclosurePanel(mainLayout, this);
        setCompositionRoot(disclosurePanel);
    }

    public void setOpen(boolean open) { }

    public abstract void setDisclosureCaption();

    public void setDisclosureCaption(String disclosureCaption)
    {
        disclosurePanel.setCaption(disclosureCaption);
    }
}
