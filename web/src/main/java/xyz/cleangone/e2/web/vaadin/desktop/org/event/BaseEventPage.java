package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.vaadin.navigator.View;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.*;
import xyz.cleangone.data.manager.event.ItemManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.MyUI;
import xyz.cleangone.e2.web.vaadin.desktop.org.BasePage;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.LeftColLayout;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.RightColLayout;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public abstract class BaseEventPage extends BasePage implements View
{
    protected enum PageCols { Left, Center, Right };

    protected final LeftColLayout leftLayout;
    protected final VerticalLayout centerLayout = vertical(MARGIN_R, BACK_PINK);
    protected final RightColLayout rightLayout;

    private VerticalLayout centerWrapperLayout = new VerticalLayout();

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
        if (MyUI.COLORS) { setStyleName("backBlue"); }

        centerWrapperLayout.setMargin(false);
        centerWrapperLayout.setSpacing(true);

        leftLayout = new LeftColLayout(getMainLayoutHeight(), UI.getCurrent().getPage().getBrowserWindowWidth());
        rightLayout = new RightColLayout(actionBar);

        for (PageCols pageCol : pageCols)
        {
            if (pageCol == PageCols.Left)
            {
                mainLayout.addComponent(leftLayout);
            }
            else if (pageCol == PageCols.Center)
            {
                mainLayout.addComponent(centerWrapperLayout);
                mainLayout.setExpandRatio(centerWrapperLayout, 1.0f);

                centerWrapperLayout.addComponent(centerLayout);
                centerWrapperLayout.setExpandRatio(centerLayout, 1.0f);
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

        // move rightCol to centerwrapper if too skinny for both
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> resetPageWidth());
        resetPageWidth();

        resetHeader();
        return set();
    }

    protected abstract PageDisplayType set();

    private void resetPageWidth()
    {
        mainLayout.removeComponent(rightLayout);
        centerWrapperLayout.removeComponent(rightLayout);

        if (UI.getCurrent().getPage().getBrowserWindowWidth() < 800) { centerWrapperLayout.addComponent(rightLayout); }
        else { mainLayout.addComponent(rightLayout); }
    }
}