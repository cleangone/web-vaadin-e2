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

import static xyz.cleangone.e2.web.vaadin.util.PageUtils.getMarginLayout;
import static xyz.cleangone.e2.web.vaadin.util.PageUtils.setNavStyle;

public abstract class BaseProfilePage extends BasePage implements View
{
    // todo - hack on style - put this somewhere appropriate
    protected static String STYLE_FONT_BOLD = "fontBold";
    protected static String STYLE_LINK = "link";
    protected static String STYLE_LINK_ACTIVE = "linkActive";

    private ProfilePageType currPageType;
    private final HorizontalLayout leftWrapper = new HorizontalLayout();
    private final VerticalLayout leftLayout = new VerticalLayout();
    private final VerticalLayout centerLayout = new VerticalLayout();

    protected Organization org;
    protected final Map<ProfilePageType, BaseAdmin> components = new HashMap<>();

    public BaseProfilePage(ProfilePageType currPageType)
    {
        super(new HorizontalLayout(), BannerStyle.Single);  // mainLayout is horizontal - nav col and main content
        this.currPageType = currPageType;

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

    protected abstract Component getLinksLayout();

    protected VerticalLayout getLinksLayout(ProfilePageType... profilePageTypes)
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);

        for (ProfilePageType profilePageType : profilePageTypes)
        {
            layout.addComponent(getLink(profilePageType));
        }

        return layout;
    }

    protected Component getLink(ProfilePageType pageType)
    {
        return getLink(pageType, STYLE_LINK_ACTIVE, STYLE_LINK);
    }

    private void set()
    {
        addStyles();

        leftLayout.removeAllComponents();
        leftLayout.addComponent(getLinksLayout());

        centerLayout.removeAllComponents();
        centerLayout.addComponent(components.get(currPageType));
    }

    private void addStyles()
    {
        String textColor = VaadinUtils.getOrDefault(org.getNavTextColor(), "black");
        String selectedTextColor = VaadinUtils.getOrDefault(org.getNavSelectedTextColor(), "black");

        Page.Styles styles = Page.getCurrent().getStyles();

        String textStyleName = "category-text-" + org.getTag();
        styles.add("." + textStyleName + " {color: " + textColor + "}");

        String selectedTextStyleName = "category-text-selected-" + org.getTag();
        styles.add("." + selectedTextStyleName + " {color: " + selectedTextColor + "}");
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

    private void setMenuLeftStyle(Organization org)
    {
        leftWrapper.setStyleName(setNavStyle("menu-left-", org));
    }
}