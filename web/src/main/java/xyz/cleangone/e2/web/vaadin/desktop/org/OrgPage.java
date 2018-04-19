package xyz.cleangone.e2.web.vaadin.desktop.org;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

import com.vaadin.navigator.View;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.e2.web.manager.SessionManager;

import java.util.List;

public class OrgPage extends BasePage implements View
{
    private static String STYLE_ARTICLE_BOTTOM = "link wordWrap backgroundWhite";
    private static String STYLE_ARTICLE = STYLE_ARTICLE_BOTTOM + " dividerBot";

    private static int LEFT_WIDTH_DEFAULT = 550;
    private static int CENTER_RIGHT_WIDTH_DEFAULT = 250;

    public static final String NAME = "Org";

    private Organization org;
    private List<OrgEvent> events;

    private int pageWidth;
    private int min2ColPageWidth = LEFT_WIDTH_DEFAULT + CENTER_RIGHT_WIDTH_DEFAULT;
    private int min3ColPageWidth = min2ColPageWidth + CENTER_RIGHT_WIDTH_DEFAULT;

    public OrgPage()
    {
        super(BannerStyle.Carousel);
    }

    protected String getPageName()
    {
        return "Main Page";
    }

    protected PageDisplayType set(SessionManager sessionManager)
    {
        super.set(sessionManager);
        sessionManager.resetEventManager();

        org = orgMgr.getOrg();
        resetHeader();

        return set();
    }

    protected PageDisplayType set()
    {
        if (changeManager.unchanged(org) &&
            changeManager.unchanged(events.size()) &&
            changeManager.unchangedEntity(org) &&
            changeManager.unchangedEntity(events))
        {
            return PageDisplayType.NoChange;
        }

        events = sessionMgr.getResetEventManager().getEvents();
        changeManager.reset(org, events.size());
        mainLayout.removeAllComponents();

        String introHtml = org.getIntroHtml();
        if (introHtml != null)
        {
            VerticalLayout introLayout = new VerticalLayout();
            introLayout.setMargin(new MarginInfo(false, true, false, true)); // T/R/B/L margins
            introLayout.addComponent(getHtmlLabel(introHtml));
            mainLayout.addComponent(introLayout);
        }

        HorizontalLayout orgLayout = new HorizontalLayout();
        orgLayout.setWidth("100%");
        orgLayout.setMargin(false);
        orgLayout.setSpacing(true);
//        orgLayout.addStyleName("backYellow");

        setOrgLayout(orgLayout);
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> setOrgLayout(orgLayout, e.getWidth()));

        mainLayout.addComponent(orgLayout);
        return PageDisplayType.ObjectRetrieval;
    }

    // set orglayout if page has crossed a width boundary and must be laid out differently
    private void setOrgLayout(HorizontalLayout orgLayout, int newPageWidth)
    {
        if ((pageWidth < min2ColPageWidth && newPageWidth > min2ColPageWidth) ||
            (pageWidth > min2ColPageWidth && pageWidth < min3ColPageWidth &&
                (newPageWidth < min2ColPageWidth || newPageWidth > min3ColPageWidth)) ||
            (pageWidth > min3ColPageWidth && newPageWidth < min3ColPageWidth))
        {
            setOrgLayout(orgLayout);
        }
    }

    private void setOrgLayout(HorizontalLayout orgLayout)
    {
        orgLayout.removeAllComponents();

        int leftWidth = LEFT_WIDTH_DEFAULT;
        int centerWidth = CENTER_RIGHT_WIDTH_DEFAULT;
        int rightWidth = CENTER_RIGHT_WIDTH_DEFAULT;

        if (org.colWidthsSet())
        {
            if (org.getLeftColWidth() != 0) { leftWidth = org.getLeftColWidth(); }
            centerWidth = org.getCenterColWidth();
            rightWidth = org.getRightColWidth();

            min2ColPageWidth = leftWidth + centerWidth + 20;
            min3ColPageWidth = min2ColPageWidth + rightWidth + 40;
        }

        boolean useCenterCol = centerWidth > 0;
        boolean useRightCol = rightWidth > 0;

        pageWidth = UI.getCurrent().getPage().getBrowserWindowWidth();
        if (pageWidth < min3ColPageWidth) { useRightCol = false; }
        if (pageWidth < min2ColPageWidth) { useCenterCol = false; }
        if (!useRightCol) { centerWidth = Math.max(centerWidth, rightWidth); }

        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.setWidth(leftWidth, Unit.PIXELS);
        leftLayout.setMargin(true);
//        leftLayout.addStyleName("backBlue");

        VerticalLayout centerLayout = new VerticalLayout();
        centerLayout.setMargin(new MarginInfo(true, true, true, false)); // T/R/B/L margins
        centerLayout.setWidth(centerWidth, Unit.PIXELS);
//        centerLayout.addStyleName("backOrange");

        VerticalLayout rightLayout = new VerticalLayout();
        rightLayout.setWidth(rightWidth, Unit.PIXELS);
        rightLayout.setMargin(new MarginInfo(true, true, true, false)); // T/R/B/L margins
//        rightLayout.addStyleName("backGreen");

        OrgEvent bottomLeftEvent = null;
        OrgEvent bottomCenterEvent = null;
        OrgEvent bottomRightEvent = null;

        // events are sorted by displayOrder
        for (OrgEvent event : events)
        {
            if (event.getEnabled())
            {
                if (useRightCol && event.getDisplayCol() == OrgEvent.ColType.RightCol) {
                    bottomRightEvent = event;
                }
                else if (useCenterCol && event.getDisplayCol() != OrgEvent.ColType.LeftCol ) {
                    bottomCenterEvent = event;
                }
                else { bottomLeftEvent = event; }
            }
        }

        for (OrgEvent event : events)
        {
            if (event.getEnabled())
            {
                if (useRightCol && event.getDisplayCol() == OrgEvent.ColType.RightCol)
                {
                    rightLayout.addComponent(getEventBlurb(event, bottomRightEvent));
                }
                else if (useCenterCol && event.getDisplayCol() != OrgEvent.ColType.LeftCol )
                {
                    centerLayout.addComponent(getEventBlurb(event, bottomCenterEvent));
                }
                else
                {
                    leftLayout.addComponent(getEventBlurb(event, bottomLeftEvent));
                }
            }
        }

        orgLayout.addComponent(leftLayout);
        orgLayout.setExpandRatio(leftLayout, 1.0f);
        if (useCenterCol) { orgLayout.addComponent(centerLayout); }
        if (useRightCol) { orgLayout.addComponent(rightLayout); }
    }

    private Component getEventBlurb(OrgEvent event, OrgEvent bottomEvent)
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);

        Label label = getHtmlLabel(event.getBlurbHtml());
        label.setStyleName(event == bottomEvent ? STYLE_ARTICLE_BOTTOM : STYLE_ARTICLE);

        layout.addComponent(label);
        layout.addLayoutClickListener(e -> navigateTo(event));

        return(layout);
    }
}
