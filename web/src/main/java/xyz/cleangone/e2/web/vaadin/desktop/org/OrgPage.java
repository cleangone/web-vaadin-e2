package xyz.cleangone.e2.web.vaadin.desktop.org;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

import com.vaadin.navigator.View;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.e2.web.manager.EntityChangeManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.util.PageUtils;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class OrgPage extends BasePage implements View
{
    private static String STYLE_ARTICLE_BOTTOM = "link wordWrap backgroundWhite";
    private static String STYLE_ARTICLE = STYLE_ARTICLE_BOTTOM + " dividerBot";

    private static int LEFT_WIDTH_DEFAULT = 550;
    private static int CENTER_RIGHT_WIDTH_DEFAULT = 250;

    private static final Logger LOG = Logger.getLogger(OrgPage.class.getName());
    public static final String NAME = "Org";

    private Organization org;
    private List<OrgEvent> events;
    private EntityChangeManager changeManager = new EntityChangeManager();

    private int pageWidth;
    private int oneColPageWidth = LEFT_WIDTH_DEFAULT + CENTER_RIGHT_WIDTH_DEFAULT;
    private int twoColPageWidth = oneColPageWidth + CENTER_RIGHT_WIDTH_DEFAULT;

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
        orgLayout.setSpacing(false);

        setOrgLayout(orgLayout);
        pageWidth = UI.getCurrent().getPage().getBrowserWindowWidth();
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> {
            int newPageWidth = e.getWidth();

            if ((pageWidth < oneColPageWidth && newPageWidth > oneColPageWidth) ||
                (pageWidth > oneColPageWidth && pageWidth < twoColPageWidth &&
                    (newPageWidth < oneColPageWidth || newPageWidth > twoColPageWidth)) ||
                (pageWidth > twoColPageWidth && newPageWidth < twoColPageWidth))
            {
                setOrgLayout(orgLayout);
            }

            pageWidth = newPageWidth;
        });

        mainLayout.addComponent(orgLayout);
        return PageDisplayType.ObjectRetrieval;
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

            oneColPageWidth = leftWidth + centerWidth;
            twoColPageWidth = oneColPageWidth + rightWidth;
        }

        boolean useCenterCol = centerWidth > 0;
        boolean useRightCol = rightWidth > 0;

        int windowWidth = UI.getCurrent().getPage().getBrowserWindowWidth();
        if (windowWidth < twoColPageWidth) { useRightCol = false; }
        if (windowWidth < oneColPageWidth) { useCenterCol = false; }

        VerticalLayout leftLayout = new VerticalLayout();

        VerticalLayout centerLayout = new VerticalLayout();
        centerLayout.setMargin(new MarginInfo(true, false, true, false)); // T/R/B/L margins
        centerLayout.setWidth(centerWidth, Unit.PIXELS);

        VerticalLayout rightLayout = new VerticalLayout();
        rightLayout.setWidth(rightWidth, Unit.PIXELS);

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
                if (useRightCol && event.getDisplayCol() == OrgEvent.ColType.RightCol) {
                    rightLayout.addComponent(getEventBlurb(event, bottomRightEvent));
                }
                else if (useCenterCol && event.getDisplayCol() != OrgEvent.ColType.LeftCol ) {
                    centerLayout.addComponent(getEventBlurb(event, bottomCenterEvent));
                }
                else {
                    leftLayout.addComponent(getEventBlurb(event, bottomLeftEvent));
                }
            }
        }

        orgLayout.addComponents(leftLayout);
        if (useCenterCol) { orgLayout.addComponent(centerLayout); }
        if (useRightCol) { orgLayout.addComponent(rightLayout); }
        orgLayout.setExpandRatio(leftLayout, 1.0f);
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
