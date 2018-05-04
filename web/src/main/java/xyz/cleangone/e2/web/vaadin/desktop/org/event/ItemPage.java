package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.vaadin.navigator.View;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.ItemLayout;
import xyz.cleangone.e2.web.vaadin.desktop.org.profile.BidsPage;

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
        centerLayout.removeAllComponents();
        centerLayout.addComponent(new ItemLayout(item, category, event, bidHandler, sessionMgr, actionBar, e -> closeItem()));
    }

    // todo - hack - watchLaout should open itemLayout in same page, not the ItemPage
    private void closeItem()
    {
        if (closeToWatch)
        {
            getUI().getNavigator().navigateTo(BidsPage.WATCH_NAME);
        }
        else
        {
            String viewName = CatalogPage.NAME + "-" + category.getName();
            getUI().getNavigator().addView(viewName, new CatalogPage());
            getUI().getNavigator().navigateTo(viewName);
        }
    }

    public void setCloseToWatch()
    {
        closeToWatch = true;
    }
}