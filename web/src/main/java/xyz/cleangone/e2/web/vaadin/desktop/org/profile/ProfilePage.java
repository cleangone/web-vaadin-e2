package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.org.BasePage;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.HashMap;
import java.util.Map;
import static xyz.cleangone.e2.web.vaadin.util.PageUtils.*;


public class ProfilePage extends BasePage implements View
{
    public static final String NAME = "UserProfile";
    public static final String DISPLAY_NAME = "User Profile";

    private static String STYLE_FONT_BOLD = "fontBold";

    // quick hack on style
    private static String STYLE_LINK = "link";
    private static String STYLE_LINK_ACTIVE = "linkActive";

    private final HorizontalLayout leftWrapper = new HorizontalLayout();
    protected final VerticalLayout leftLayout = new VerticalLayout();
    protected final VerticalLayout centerLayout = new VerticalLayout();

    private Organization org;
    private final Map<ProfilePageType, BaseAdmin> components = new HashMap<>();
    protected ProfilePageType currPageType = ProfilePageType.GENERAL;

    public ProfilePage()
    {
        super(new HorizontalLayout(), BannerStyle.Single);  // mainLayout is horizontal - nav col and main content

        mainLayout.setMargin(false);
        mainLayout.setSizeFull();

        leftWrapper.setMargin(false);
        leftWrapper.setSpacing(false);
        leftWrapper.setSizeUndefined();
        leftWrapper.addComponents(getMarginLayout(getMainLayoutHeight()), leftLayout);
        if (COLORS) { leftWrapper.addStyleName("backRed"); }

        leftLayout.setSpacing(false);
        leftLayout.setWidthUndefined();
        leftLayout.setMargin(new MarginInfo(true, true, true, false)); // T/R/B/L margins
        leftLayout.setHeight("100%");

        centerLayout.setMargin(new MarginInfo(false, true, false, true)); // T/R/B/L margins
        centerLayout.setHeight("100%");
        if (COLORS) { centerLayout.addStyleName("backBlue"); }

        components.put(ProfilePageType.GENERAL,     new ProfileAdmin(actionBar));
        components.put(ProfilePageType.BIDS,        new BidsAdmin(actionBar));
        components.put(ProfilePageType.DONATIONS,   new ActionsAdmin(actionBar, ProfilePageType.DONATIONS));
        components.put(ProfilePageType.PURCHASES,   new ActionsAdmin(actionBar, ProfilePageType.PURCHASES));
        components.put(ProfilePageType.BID_HISTORY, new ActionsAdmin(actionBar, ProfilePageType.BIDS));

        mainLayout.addComponents(leftWrapper, centerLayout);
        mainLayout.setExpandRatio(centerLayout, 1.0f);
    }

    public PageDisplayType set(SessionManager sessionMgr)
    {
        super.set(sessionMgr);
        sessionMgr.resetEventManager();
        org = orgMgr.getOrg();

        UI ui = getUI();
        for (BaseAdmin component : components.values())
        {
            component.set(sessionMgr, ui);
        }

        setMenuLeftStyle(org);
        resetHeader();
        set();

        return PageDisplayType.NotApplicable;
    }

    private void set()
    {
        setLeftLayout();
        setCenterLayout();
    }

    private void setLeftLayout()
    {
        leftLayout.removeAllComponents();
        leftLayout.addComponent(getLinksLayout());
    }

    private void setCenterLayout()
    {
        centerLayout.removeAllComponents();
        centerLayout.addComponent(components.get(currPageType));
    }

    private Component getLinksLayout()
    {
        String textColor = VaadinUtils.getOrDefault(org.getNavTextColor(), "black");
        String selectedTextColor = VaadinUtils.getOrDefault(org.getNavSelectedTextColor(), "black");

        Page.Styles styles = Page.getCurrent().getStyles();

        String textStyleName = "category-text-" + org.getTag();
        styles.add("." + textStyleName + " {color: " + textColor + "}");

        String selectedTextStyleName = "category-text-selected-" + org.getTag();
        styles.add("." + selectedTextStyleName + " {color: " + selectedTextColor + "}");

        VerticalLayout linkLayout = new VerticalLayout();
        linkLayout.setMargin(false);
        linkLayout.setSpacing(false);

        linkLayout.addComponent(getLink(ProfilePageType.GENERAL));
        linkLayout.addComponent(getLink(ProfilePageType.BIDS));
        linkLayout.addComponent(new Label(""));
        linkLayout.addComponent(getLink(ProfilePageType.DONATIONS));
        linkLayout.addComponent(getLink(ProfilePageType.PURCHASES));
        linkLayout.addComponent(getLink(ProfilePageType.BID_HISTORY));

        Label label = new Label("User Profile");
        label.setStyleName(STYLE_FONT_BOLD);

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.addComponents(label, linkLayout);

        return layout;
    }

    private Component getLink(ProfilePageType pageType)
    {
        return getLink(pageType, STYLE_LINK_ACTIVE, STYLE_LINK);
    }
    private Component getLink(ProfilePageType pageType, String selectedTextStyleName, String textStyleName)
    {
        String styleName = currPageType == pageType ? selectedTextStyleName : textStyleName;
        return VaadinUtils.getLayout(pageType.toString(), styleName, e -> setPage(pageType));
    }

    private void setPage(ProfilePageType pageType)
    {
        if (pageType != currPageType)
        {
            currPageType = pageType;
            set();
        }
    }

    protected void setMenuLeftStyle(Organization org)
    {
        leftWrapper.setStyleName(setNavStyle("menu-left-", org));
    }
}