package xyz.cleangone.e2.web.vaadin.util.disclosure;

import com.vaadin.ui.Component;
import org.vaadin.viritin.components.DisclosurePanel;
import xyz.cleangone.e2.web.vaadin.util.disclosure.BaseDisclosure;

// provide a hook into open/close
public class ControllableDisclosurePanel extends DisclosurePanel
{
    private BaseDisclosure parent;

    public ControllableDisclosurePanel(Component content, BaseDisclosure parent)
    {
        super("", content);
        this.parent = parent;
    }

    public DisclosurePanel setOpen(boolean open)
    {
        parent.setOpen(open);
        return super.setOpen(open);
    }

}
