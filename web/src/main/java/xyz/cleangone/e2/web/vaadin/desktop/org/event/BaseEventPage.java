package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.vaadin.navigator.View;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.*;
import xyz.cleangone.data.manager.event.ItemManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.BasePage;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.LeftColLayout;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.RightColLayout;


public abstract class BaseEventPage extends BasePage implements View
{
    protected enum PageCols { Left, Center, Right };

    protected final LeftColLayout leftLayout = new LeftColLayout();
    protected final VerticalLayout centerLayout = new VerticalLayout();
    protected final RightColLayout rightLayout;

    protected EventManager eventMgr;
    protected ItemManager itemMgr;
    protected UserManager userMgr;
    protected TagManager tagMgr;
    protected OrgEvent event;
    protected User user;

    public BaseEventPage()
    {
        this(PageCols.Left, PageCols.Center, PageCols.Right);
    }

    public BaseEventPage(PageCols... pageCols)
    {
        super(new HorizontalLayout(), BannerStyle.Single);  // mainLayout is horizontal
        mainLayout.setWidth("100%");
        mainLayout.setHeightUndefined();
        mainLayout.setMargin(false);

        centerLayout.setMargin(new MarginInfo(false, true, false, true)); // T/R/B/L margins
        rightLayout = new RightColLayout(actionBar);

        for (PageCols pageCol : pageCols)
        {
            if (pageCol == PageCols.Left) { mainLayout.addComponent(leftLayout); }
            else if (pageCol == PageCols.Center)
            {
                mainLayout.addComponent(centerLayout);
                mainLayout.setExpandRatio(centerLayout, 1.0f);
            }
            else if (pageCol == PageCols.Right) { mainLayout.addComponent(rightLayout); }
        }
    }
    protected PageDisplayType set(SessionManager sessionMgr)
    {
        super.set(sessionMgr);
        eventMgr = sessionMgr.getEventManager();
        itemMgr = eventMgr.getItemManager();
        userMgr = sessionMgr.getUserManager();
        tagMgr = orgMgr.getTagManager();

        event = eventMgr.getEvent();
        user = userMgr.getUser();

        leftLayout.set(sessionMgr);
        rightLayout.set(sessionMgr);

        resetHeader();
        return set();
    }

    protected abstract PageDisplayType set();
}