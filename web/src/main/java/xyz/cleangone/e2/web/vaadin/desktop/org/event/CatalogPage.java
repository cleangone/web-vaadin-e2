package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.vaadin.navigator.View;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.CatalogLayout;

import java.util.*;
import java.util.stream.Collectors;

public class CatalogPage extends BaseEventPage implements View, CatalogView
{
    public static final String NAME = "Catalog";

    protected OrgTag category;
    protected BidHandler bidHandler;

    private CatalogLayout catalogLayout;
    private Set<String> itemIds = Collections.emptySet();

    public CatalogPage()
    {
         super(PageCols.Left, PageCols.Center);
    }

    public PageDisplayType set()
    {
        category = eventMgr.getCategory();
        if (category == null) { return PageDisplayType.NotApplicable; }  // shouldn't happen - nav back to event?

        bidHandler = new BidHandler(this, sessionMgr, actionBar);

        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> resetPageWidth());

        leftLayout.set(category);
        setCenterLayout();

        return PageDisplayType.NotApplicable;
    }

    public boolean hasItemId(String itemId)
    {
        return (itemIds.contains(itemId));
    }

    public void setCatalogLayout()
    {
        setCenterLayout();
    }
    protected void setCenterLayout()
    {
        centerLayout.removeAllComponents();

        List<CatalogItem> items = itemMgr.getItems(category.getId());
        List<CatalogItem> visibleItems = items.stream()
            .filter(CatalogItem::isVisible)
            .collect(Collectors.toList());
        itemIds = visibleItems.stream()
            .map(CatalogItem::getId)
            .collect(Collectors.toSet());

        catalogLayout = new CatalogLayout(UI.getCurrent().getPage().getBrowserWindowWidth(), leftLayout.getColWidth(), sessionMgr);
        for (CatalogItem item : visibleItems)
        {
            catalogLayout.addItem(item, user, getQuickBidButton(item));
        }

        centerLayout.addComponent(catalogLayout);
    }

    private void resetPageWidth()
    {
        catalogLayout.resetPageWidth(UI.getCurrent().getPage().getBrowserWindowWidth());
    }

    protected Button getQuickBidButton(CatalogItem item)
    {
        return bidHandler.getQuickBidButton(item);
    }
}