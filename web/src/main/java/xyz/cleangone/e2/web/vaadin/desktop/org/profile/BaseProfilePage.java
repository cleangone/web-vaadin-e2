package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.org.BasePage;
import xyz.cleangone.web.vaadin.ui.PageDisplayType;
import xyz.cleangone.web.vaadin.util.VaadinUtils;

import java.util.HashMap;
import java.util.Map;

import static xyz.cleangone.web.vaadin.util.PageUtils.*;
import static xyz.cleangone.web.vaadin.util.VaadinUtils.*;

public abstract class BaseProfilePage extends BasePage implements View
{
    // todo - hack on style - put this somewhere appropriate
    protected static String STYLE_FONT_BOLD = "fontBold";
    protected static String STYLE_LINK = "link";
    protected static String STYLE_LINK_ACTIVE = "linkActive";

    private ProfilePageType currPageType;
    private final HorizontalLayout leftWrapper  = horizontal(MARGIN_FALSE, SPACING_FALSE, VaadinUtils.SIZE_UNDEFINED, BACK_RED);
    private final VerticalLayout   leftLayout   = vertical(MARGIN_TRB, SPACING_FALSE, WIDTH_UNDEFINED, HEIGHT_100_PCT);
    private final VerticalLayout   centerLayout = vertical(MARGIN_LR, HEIGHT_100_PCT, BACK_BLUE);

    protected Organization org;
    protected final Map<ProfilePageType, BaseAdmin> components = new HashMap<>();

    public BaseProfilePage(ProfilePageType currPageType)
    {
        super(new HorizontalLayout(), BannerStyle.Single);  // mainLayout is horizontal - nav col and main content
        this.currPageType = currPageType;

        mainLayout.setMargin(false);
        mainLayout.setSizeFull();

        leftWrapper.addComponents(getMarginLayout(UI.getCurrent(), BANNER_HEIGHT), leftLayout);

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
        VerticalLayout layout = vertical(MARGIN_FALSE, SPACING_FALSE);
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
        leftWrapper.setStyleName(setNavStyle("menu-left-" + org.getTag(), org.getNavBackgroundColor()));
    }
}