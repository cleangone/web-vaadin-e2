package xyz.cleangone.e2.web.vaadin.desktop.org.event.components;

import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.data.manager.event.BidManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.manager.ViewStatus;
import xyz.cleangone.e2.web.vaadin.desktop.MyUI;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.ItemPage;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class CatalogLayout extends VerticalLayout
{
    private static int DEFAULT_NAV_COL_WIDTH = 200;

    private final EventManager eventMgr;
    private final TagManager tagMgr;
    private final BidManager bidMgr;
    private final ViewStatus viewStatus;

    private GridLayout catalogGridLayout = new GridLayout();
    private List<CatalogItemLayout> itemLayouts = new ArrayList<>();
    private Map<String, OrgEvent> eventsById;

    private int pageWidth;
    private int navColWidth;
    private int maxCols;
    private int currCol;
    private int currRow;

    // called by CatalogPage
    public CatalogLayout(int pageWidth, int navColWidth, SessionManager sessionMgr)
    {
        OrgManager orgMgr = sessionMgr.getOrgManager();
        eventMgr = sessionMgr.getEventManager();
        tagMgr = orgMgr.getTagManager();
        bidMgr = orgMgr.getBidManager();
        viewStatus = sessionMgr.getViewStatus();

        setLayout(this, MARGIN_FALSE, SPACING_TRUE, WIDTH_100_PCT, BACK_GREEN);
        setPageWidth(pageWidth, navColWidth);

        catalogGridLayout.setMargin(false);
        catalogGridLayout.setSpacing(true);
        catalogGridLayout.setWidth("100%");
        if (SHOW_BACKBROUND_COLORS) { catalogGridLayout.setStyleName(BACK_ORANGE); }
    }

    // called by WatchLayout
    public CatalogLayout(int pageWidth, SessionManager sessionMgr, Map<String, OrgEvent> eventsById)
    {
        this(pageWidth, DEFAULT_NAV_COL_WIDTH, sessionMgr);
        this.eventsById = eventsById;
    }

    public CatalogLayout(int pageWidth, List<CatalogItem> items, User user, SessionManager sessionMgr)
    {
        this(pageWidth, DEFAULT_NAV_COL_WIDTH, sessionMgr);

        for (CatalogItem item : items)
        {
            addItem(item, user, null);
        }
    }

    private void setPageWidth(int pageWidth, int navColWidth)
    {
        this.pageWidth = pageWidth;
        this.navColWidth = navColWidth;

        int layoutWidth = pageWidth - navColWidth;
        maxCols = Math.max(layoutWidth/200, 1); // item cols each ~200px

        catalogGridLayout.setRows(1);
        catalogGridLayout.setColumns(maxCols);
        for (int i = 0; i < maxCols; i++) { catalogGridLayout.setColumnExpandRatio(i, 1); }
    }

    public void resetPageWidth(int pageWidth)
    {
        resetPageWidth(pageWidth, navColWidth);
    }
    public void resetPageWidth(int pageWidth, int navColWidth)
    {
        if (pageWidth != this.pageWidth || navColWidth != this.navColWidth)
        {
            catalogGridLayout.removeAllComponents();
            setPageWidth(pageWidth, navColWidth);
            resetItems();
        }
    }

    private void resetItems()
    {
        catalogGridLayout.removeAllComponents();
        currCol = 0;
        currRow = 0;

        for (CatalogItemLayout itemLayout : itemLayouts) { addComponent(itemLayout); }
    }

    public void addItem(CatalogItem item, User user, Button quickBidButton)
    {
        if (getComponentCount() == 0) { addComponents(createViewSoldLayout(), catalogGridLayout); }

        CatalogItemLayout itemLayout = new CatalogItemLayout(item, user, quickBidButton, tagMgr, bidMgr,  e -> {
            // LayoutClickListener
            // this needs to do different things for catalog and watched items

            ItemPage itemPage = new ItemPage();

            if (eventsById != null)
            {
                eventMgr.setEvent(eventsById.get(item.getEventId()));
                eventMgr.setCategory(getCategory(item));
                itemPage.setCloseToWatch();
            }

            eventMgr.setItem(item);
            getUI().getNavigator().addView(ItemPage.NAME, itemPage);
            getUI().getNavigator().navigateTo(ItemPage.NAME);
        });

        itemLayouts.add(itemLayout);

        addComponent(itemLayout);
    }

    // todo - hack
    private OrgTag getCategory(CatalogItem item)
    {
        for (OrgTag category : tagMgr.getCategories())
        {
           if (item.getCategoryIds().contains(category.getId())) { return category; }
        }

        return null;
    }

    private void addComponent(CatalogItemLayout itemLayout)
    {
        if (itemLayout.isSold() && !viewStatus.getViewSold()) { return; }

        int extraItemWidth = (itemLayout.getRelativeWidth() == null || itemLayout.getRelativeWidth() == 1) ? 0 : 1; // max out at double col width
        if (extraItemWidth >= maxCols) { extraItemWidth = 0; }  // for now, this means only have one col

        int spanCol = currCol + extraItemWidth;
        if (spanCol >= maxCols)
        {
            addRow();
            spanCol = currCol + extraItemWidth;
        }

        catalogGridLayout.addComponent(itemLayout, currCol, currRow, spanCol, currRow);

        currCol = spanCol + 1;
        if (currCol >= maxCols) { addRow(); }
    }

    private void addRow()
    {
        currCol = 0;
        currRow++;
        catalogGridLayout.setRows(currRow + 1);
    }

    private VerticalLayout createViewSoldLayout()
    {
        CheckBox checkBox = VaadinUtils.createCheckBox("View Sold", viewStatus.getViewSold());
        checkBox.addValueChangeListener(event -> {
            viewStatus.setViewSold(event.getValue());
            resetItems();
        });

        return vertical(checkBox, MARGIN_TL);
    }
}