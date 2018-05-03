package xyz.cleangone.e2.web.vaadin.desktop.org.event.components;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.event.BidManager;
import xyz.cleangone.e2.web.vaadin.desktop.MyUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CatalogLayout extends GridLayout
{
    private final EventManager eventMgr;
    private final BidManager bidManager;

    private List<CatalogItemLayout> itemLayouts = new ArrayList<>();
    private Map<String, OrgEvent> eventsById;

    private int pageWidth;
    private int maxCols;
    private int currCol;
    private int currRow;

    public CatalogLayout(int pageWidth, EventManager eventMgr, BidManager bidManager)
    {
        this.eventMgr = eventMgr;
        this.bidManager = bidManager;

        setWidth("100%");
        setMargin(new MarginInfo(true, false, false, false)); // T/R/B/L margins
        setSpacing(true);
        setPageWidth(pageWidth);

        if (MyUI.COLORS) { setStyleName("backOrange"); }
    }

    public CatalogLayout(int pageWidth, EventManager eventMgr, BidManager bidManager, Map<String, OrgEvent> eventsById)
    {
        this(pageWidth, eventMgr, bidManager);
        this.eventsById = eventsById;
    }

    public CatalogLayout(int pageWidth, List<CatalogItem> items, User user, EventManager eventMgr, BidManager bidManager)
    {
        this(pageWidth, eventMgr, bidManager);

        for (CatalogItem item : items)
        {
            addItem(item, user, null);
        }
    }

    private void setPageWidth(int pageWidth)
    {
        this.pageWidth = pageWidth;
        int layoutWidth = pageWidth - 200;
        maxCols = layoutWidth / 200;
        currCol = 0;
        currRow = 0;

        setRows(1);
        setColumns(maxCols);
        for (int i = 0; i < maxCols; i++) { setColumnExpandRatio(i, 1); }
    }

    public void resetPageWidth(int pageWidth)
    {
        if (pageWidth != this.pageWidth)
        {
            removeAllComponents();
            setPageWidth(pageWidth);
            for (CatalogItemLayout itemLayout : itemLayouts) { addComponent(itemLayout); }
        }
    }

    public void addItem(CatalogItem item, User user, Button quickBidButton)
    {
        CatalogItemLayout itemLayout = new CatalogItemLayout(item, user, quickBidButton, eventMgr, bidManager, eventsById);
        itemLayouts.add(itemLayout);

        addComponent(itemLayout);
    }

    private void addComponent(CatalogItemLayout itemLayout)
    {
        int extraItemWidth = (itemLayout.getRelativeWidth() == null || itemLayout.getRelativeWidth() == 1) ? 0 : 1; // max out at double col width
        if (extraItemWidth >= maxCols) { extraItemWidth = 0; }  // for now, this means only have one col

        int spanCol = currCol + extraItemWidth;
        if (spanCol >= maxCols)
        {
            addRow();
            spanCol = currCol + extraItemWidth;
        }

        addComponent(itemLayout, currCol, currRow, spanCol, currRow);

        currCol = spanCol + 1;
        if (currCol >= maxCols) { addRow(); }
    }

    private void addRow()
    {
        currCol = 0;
        currRow++;
        setRows(currRow + 1);
    }
}