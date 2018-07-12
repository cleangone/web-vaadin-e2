package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.vaadin.navigator.View;
import com.vaadin.ui.UI;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.web.vaadin.ui.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.ItemLayout;

public class ItemPage extends CatalogPage implements View
{
    public static final String NAME = "Item";

    private boolean closeToWatch;
    private CatalogItem item;

    public void reset()
    {
        itemMgr.setItem(itemMgr.getItemById(item.getId()));
        set();
    }

    public PageDisplayType set()
    {
        // todo - hack to not call super.set()
        category = eventMgr.getCategory();
        item = itemMgr.getItem();

        bidHandler = new BidHandler(this, sessionMgr, actionBar);

        // todo - another hack that shows ItemPage not really a child of CatalogPage
        if (sessionMgr.isMobileBrowser())
        {
            mainLayout.removeComponent(leftLayout);
        }
        else { leftLayout.set(category); }

        setCenterLayout();

        return PageDisplayType.NotApplicable;
    }

    public boolean hasItemId(String itemId)
    {
        return (item != null && item.getId().equals(itemId));
    }

    protected void setCenterLayout()
    {
        int height = UI.getCurrent().getPage().getBrowserWindowHeight();

        centerLayout.removeAllComponents();
        centerLayout.addComponent(new ItemLayout(item, category, event, height, bidHandler, sessionMgr, actionBar, closeToWatch));
    }

    public void setCloseToWatch()
    {
        closeToWatch = true;
    }
}