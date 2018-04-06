package xyz.cleangone.e2.web.vaadin.desktop.actionbar;

import com.vaadin.server.Page;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.PageUtils;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class ActionBar extends HorizontalLayout implements MessageDisplayer
{
    public static String ACTION_BAR_STYLE_NAME = "actionBarMain";
    private static String DEFAULT_BACKGROUND_COLOR = "whitesmoke";

    private LeftMenuBar leftMenuBar = new LeftMenuBar();
    private CenterMenuBar centerMenuBar = new CenterMenuBar();
    private RightMenuBar rightMenuBar = new RightMenuBar();

    public ActionBar()
    {
        setWidth("100%");
        setMargin(false);
        setSpacing(false);
        setStyleName(ACTION_BAR_STYLE_NAME);

        HorizontalLayout leftLayout = getLayout(leftMenuBar, "10%");
        HorizontalLayout centerLayout = getLayout(centerMenuBar, "50%");
        HorizontalLayout rightLayout = getLayout(rightMenuBar, "40%");
        rightLayout.addComponent(getHtmlLabel(""));

        addComponents(leftLayout, centerLayout, rightLayout);
        setComponentAlignment(rightLayout, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));
    }

    private HorizontalLayout getLayout(MenuBar menuBar, String pct)
    {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth(pct);
        layout.addComponent(menuBar);
        return layout;
    }

    public PageDisplayType set(SessionManager sessionMgr)
    {
        setStyle(sessionMgr);
        PageDisplayType leftDisplayType   = leftMenuBar.set(sessionMgr);
        PageDisplayType centerDisplayType = centerMenuBar.set(sessionMgr);
        PageDisplayType rightDisplayType  = rightMenuBar.set(sessionMgr);

        return PageUtils.getPageDisplayType(leftDisplayType, centerDisplayType, rightDisplayType);
    }

    public void displayMessage(String msg)
    {
        centerMenuBar.displayMessage(msg);
    }
    public void setCartMenuItem()
    {
        rightMenuBar.setCartMenuItem();
    }

    public MenuBar.MenuItem override(String caption)
    {
        leftMenuBar.removeItems();
        centerMenuBar.removeItems();
        rightMenuBar.removeItems();

        return leftMenuBar.addItem(caption, null, null);
    }


    public void setStyle(SessionManager sessionMgr)
    {
        BaseOrg baseOrg = sessionMgr.getOrg();
        if (baseOrg == null) { return; }

        String styleName = ACTION_BAR_STYLE_NAME + "-" + baseOrg.getTag();
        OrgEvent currEvent = sessionMgr.getEventManager().getEvent();
        if (currEvent != null)
        {
            styleName += "-" + currEvent.getTag();
            baseOrg = currEvent;
        }

        if (baseOrg.getBarBackgroundColor() != null)
        {
            addActionBarStyle(styleName, baseOrg.getBarBackgroundColor());
            setStyleName(styleName);
        }
    }


//    private String addActionBarStyle(Organization org)
//    {
//        if (org.getBarBackgroundColor() == null) { return ACTION_BAR_STYLE_NAME; }
//
//        String styleName = ACTION_BAR_STYLE_NAME + "-" + org.getTag();
//        addActionBarStyle(styleName, org.getBarBackgroundColor());
//
//        return styleName;
//    }
//
//    private String addActionBarStyle(Organization org, OrgEvent event)
//    {
//        if (event.getBarBackgroundColor() == null) { return ACTION_BAR_STYLE_NAME; }
//
//        String styleName = ACTION_BAR_STYLE_NAME + "-" + org.getTag() + "-" + event.getTag();
//        addActionBarStyle(styleName, event.getBarBackgroundColor());
//
//        return styleName;
//    }

    public static void addActionBarStyle()
    {
        addActionBarStyle(ACTION_BAR_STYLE_NAME, DEFAULT_BACKGROUND_COLOR);
    }
    public static void addActionBarStyle(String styleName, String backgroundColor)
    {
        Page.Styles styles = Page.getCurrent().getStyles();
        styles.add("." + styleName +
            " { background: " + backgroundColor + "; border-top: 1px solid silver; border-bottom: 1px solid silver; }");
    }
}
