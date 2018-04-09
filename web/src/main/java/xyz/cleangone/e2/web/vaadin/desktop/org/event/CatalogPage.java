package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.vaadin.navigator.View;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.ImageManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.CatalogLayout;

import java.util.*;

// sep page from event to support back button
public class CatalogPage extends BaseEventPage implements View
{
    public static final String NAME = "Catalog";
    private static final int ITEM_COLS = 2;  // todo - make configurable

    protected ImageManager imageMgr;
    protected OrgTag category;

    public CatalogPage()
    {
         super(PageCols.Left, PageCols.Center);
    }

    protected PageDisplayType set()
    {
        category = eventMgr.getCategory();
        if (category == null) { return PageDisplayType.NotApplicable; }  // shouldn't happen - nav back to event?

        imageMgr = itemMgr.getImageManager();

        leftLayout.set(category);
        setCenterLayout();

        return PageDisplayType.NotApplicable;
    }

    protected void setCenterLayout()
    {
        centerLayout.removeAllComponents();

        List<CatalogItem> items = itemMgr.getItems(category.getId());
        centerLayout.addComponent(new CatalogLayout(items, eventMgr, imageMgr));
    }

}