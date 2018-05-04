package xyz.cleangone.e2.web.vaadin.desktop.actionbar;

import com.vaadin.server.Page;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.e2.web.manager.SessionManager;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class BaseActionBar extends HorizontalLayout
{
    public static String ACTION_BAR_STYLE_NAME = "actionBarMain";
    private static String DEFAULT_BACKGROUND_COLOR = "whitesmoke";

    public BaseActionBar()
    {
        setLayout(this, MARGIN_FALSE, SPACING_FALSE, WIDTH_100_PCT);
        setStyleName(ACTION_BAR_STYLE_NAME);
    }

    protected HorizontalLayout getLayout(MenuBar menuBar, String pct)
    {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth(pct);
        layout.addComponent(menuBar);
        return layout;
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
