package xyz.cleangone.e2.web.vaadin.desktop.org;

import com.vaadin.navigator.View;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.e2.web.manager.SessionManager;

import java.util.List;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class OrgPage extends BasePage implements View
{
    private static String STYLE_ARTICLE_BOTTOM = "link wordWrap backgroundWhite";
    private static String STYLE_ARTICLE = STYLE_ARTICLE_BOTTOM + " dividerBot";
    private static String STYLE_LINK = "link";
    private static boolean COLORS = false;

    private static int LEFT_WIDTH_DEFAULT = 550;
    private static int CENTER_RIGHT_WIDTH_DEFAULT = 250;

    public static final String NAME = "Org";

    private Organization org;
    private List<OrgEvent> events;

    private int pageWidth;
    private int min2ColPageWidth = LEFT_WIDTH_DEFAULT + CENTER_RIGHT_WIDTH_DEFAULT;
    private int min3ColPageWidth = min2ColPageWidth + CENTER_RIGHT_WIDTH_DEFAULT;

    public OrgPage(boolean isMobileBrowser)
    {
        super(isMobileBrowser ? BannerStyle.Single : BannerStyle.Carousel);
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
        if (COLORS) { mainLayout.addStyleName("backPurple"); }

        String introHtml = org.getIntroHtml();
        if (introHtml != null)
        {
            VerticalLayout introLayout = new VerticalLayout();
            introLayout.setMargin(new MarginInfo(false, true, false, true)); // T/R/B/L margins
            introLayout.addComponent(getHtmlLabel(introHtml));
            mainLayout.addComponent(introLayout);
        }

        HorizontalLayout orgLayout = new HorizontalLayout();
        orgLayout.setMargin(false);
        orgLayout.setSpacing(false);
        if (COLORS) { orgLayout.addStyleName("backYellow"); }

        setOrgLayout(orgLayout);
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> setOrgLayout(orgLayout, e.getWidth()));

        mainLayout.addComponent(orgLayout);
        mainLayout.setComponentAlignment(orgLayout, new Alignment(AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER));
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

            min2ColPageWidth = leftWidth + centerWidth;
            min3ColPageWidth = min2ColPageWidth + rightWidth;
        }

        boolean useCenterCol = centerWidth > 0;
        boolean useRightCol = rightWidth > 0;

        pageWidth = UI.getCurrent().getPage().getBrowserWindowWidth();
        if (pageWidth < min3ColPageWidth) { useRightCol = false; }
        if (pageWidth < min2ColPageWidth) { useCenterCol = false; }

        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.setMargin(true);
        if (COLORS) { leftLayout.addStyleName("backBlue"); }
        if (sessionMgr.isMobileBrowser())
        {
            useCenterCol = false;
            useRightCol = false;
            leftLayout.setWidth(UI.getCurrent().getPage().getBrowserWindowWidth(), Unit.PIXELS);
        }
        else
        {
            leftLayout.setWidth(leftWidth, Unit.PIXELS);
        }

        VerticalLayout centerLayout = new VerticalLayout();
        centerLayout.setMargin(new MarginInfo(true, true, true, false)); // T/R/B/L margins
        centerLayout.setWidth(centerWidth, Unit.PIXELS);
        if (COLORS) { centerLayout.addStyleName("backOrange"); }

        VerticalLayout rightLayout = new VerticalLayout();
        rightLayout.setMargin(new MarginInfo(true, true, true, false)); // T/R/B/L margins
        rightLayout.setWidth(rightWidth, Unit.PIXELS);
        if (COLORS) { rightLayout.addStyleName("backGreen"); }

        // events are sorted by displayOrder - bottom ones will be last
        EventBlurbLayout bottomLeftEventLayout = null;
        EventBlurbLayout bottomCenterEventLayout = null;
        EventBlurbLayout bottomRightEventLayout = null;
        for (OrgEvent event : events)
        {
            if (event.getEnabled())
            {
                if (event.getDisplayCol() == OrgEvent.ColType.RightCol)
                {
                    if (useRightCol) { bottomRightEventLayout = addEventBlurb(event, rightLayout); }
                    else if (useCenterCol) { bottomCenterEventLayout = addEventBlurb(event, centerLayout, rightLayout); }
                    else { bottomLeftEventLayout = addEventBlurb(event, leftLayout, rightLayout); }
                }
                else if (event.getDisplayCol() == OrgEvent.ColType.CenterCol)
                {
                    if (useCenterCol) { bottomCenterEventLayout = addEventBlurb(event, centerLayout); }
                    else { bottomLeftEventLayout = addEventBlurb(event, leftLayout, centerLayout); }
                }
                else
                {
                    bottomLeftEventLayout = addEventBlurb(event, leftLayout);
                }
            }
        }

        if (bottomLeftEventLayout != null)   { bottomLeftEventLayout.setStyleNameBottom(); }
        if (bottomCenterEventLayout != null) { bottomCenterEventLayout.setStyleNameBottom(); }
        if (bottomRightEventLayout != null)  { bottomRightEventLayout.setStyleNameBottom(); }

        orgLayout.addComponent(leftLayout);
        if (useCenterCol) { orgLayout.addComponent(centerLayout); }
        if (useRightCol)  { orgLayout.addComponent(rightLayout); }
    }

    private EventBlurbLayout addEventBlurb(OrgEvent event, VerticalLayout colLayout)
    {
        return addEventBlurb(event, colLayout, colLayout);
    }

    private EventBlurbLayout addEventBlurb(OrgEvent event, VerticalLayout colLayout, VerticalLayout origColLayout)
    {
        float bannerWidth = Math.min(colLayout.getWidth(), origColLayout.getWidth());
        if (colLayout.getMargin().hasLeft())  { bannerWidth -= 37; } // approx. margin width
        if (colLayout.getMargin().hasRight()) { bannerWidth -= 37; }

        boolean isTop = colLayout.getComponentCount() == 0;
        EventBlurbLayout blurbLayout = new EventBlurbLayout(event, bannerWidth, isTop);

        colLayout.addComponent(blurbLayout);
        return blurbLayout;
    }

    class EventBlurbLayout extends VerticalLayout
    {
        Label label;

        EventBlurbLayout(OrgEvent event, float bannerWidth, boolean isTop)
        {
            setMargin(isTop ? new MarginInfo(false) : new MarginInfo(true, false, false, false)); // T/R/B/L margins
            setSpacing(false);
            addStyleName(STYLE_LINK);
            if (COLORS) { addStyleName("backRed"); }

            label = getHtmlLabel(event.getBlurbHtml());
            label.setStyleName(STYLE_ARTICLE);

            if (event.getBlurbBannerUrl() != null)
            {
                addComponent(getHtmlLabel("<img src=" + event.getBlurbBannerUrl() + " width=" + bannerWidth + ">"));
            }

            addComponent(label);
            addLayoutClickListener(e -> navigateTo(event));
        }

        void setStyleNameBottom()
        {
            label.setStyleName(STYLE_ARTICLE_BOTTOM);
        }
    }
}
