package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import com.vaadin.event.LayoutEvents;
import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.manager.ImageManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.org.BasePage;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.HashMap;
import java.util.Map;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class ProfilePage extends BasePage implements View
{
    public static final String NAME = "UserProfile";
    public static final String DISPLAY_NAME = "User Profile";

    private static String STYLE_FONT_BOLD = "fontBold";

    // quick hack on style
    private static String STYLE_LINK = "link";
    private static String STYLE_LINK_ACTIVE = "linkActive";


    static int COL_MIN_HEIGHT = 700;

    private final HorizontalLayout leftWrapper = new HorizontalLayout();
    protected final VerticalLayout leftLayout = new VerticalLayout();
    protected final VerticalLayout centerLayout = new VerticalLayout();

    private Organization org;

    private final Map<ProfilePageType, BaseAdmin> components = new HashMap<>();

    protected ProfilePageType currPageType = ProfilePageType.GENERAL;

    public ProfilePage()
    {
        super(new HorizontalLayout(), BannerStyle.Single);  // mainLayout is horizontal
        mainLayout.setWidth("100%");
        mainLayout.setHeightUndefined();
        mainLayout.setMargin(false);

        leftWrapper.setMargin(false);
        leftWrapper.setSpacing(false);
        leftWrapper.addComponents(getMarginLayout(), leftLayout);

        leftLayout.setSpacing(false);
        leftLayout.setWidthUndefined();
        leftLayout.setMargin(new MarginInfo(true, true, true, false)); // T/R/B/L margins

        centerLayout.setMargin(new MarginInfo(false, true, false, true)); // T/R/B/L margins

        components.put(ProfilePageType.GENERAL, new ProfileAdmin(actionBar));
        components.put(ProfilePageType.DONATIONS, new ActionsAdmin(actionBar, ProfilePageType.DONATIONS));
        components.put(ProfilePageType.PURCHASES, new ActionsAdmin(actionBar, ProfilePageType.PURCHASES));

        mainLayout.addComponents(leftWrapper, centerLayout);
        mainLayout.setExpandRatio(centerLayout, 1.0f);
    }

    public PageDisplayType set(SessionManager sessionMgr)
    {
        super.set(sessionMgr);
        sessionMgr.resetEventManager();
        org = orgMgr.getOrg();

        for (BaseAdmin component : components.values())
        {
            component.set(sessionMgr);
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

        leftWrapper.removeAllComponents();
        leftWrapper.addComponents(getMarginLayout(), leftLayout);
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

        linkLayout.addComponent(getLink(ProfilePageType.GENERAL, STYLE_LINK_ACTIVE, STYLE_LINK));
        linkLayout.addComponent(getLink(ProfilePageType.DONATIONS, STYLE_LINK_ACTIVE, STYLE_LINK));
        linkLayout.addComponent(getLink(ProfilePageType.PURCHASES, STYLE_LINK_ACTIVE, STYLE_LINK));

        Label label = new Label("User Profile");
        label.setStyleName(STYLE_FONT_BOLD);

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.addComponents(label, linkLayout);

        return layout;
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

    // todo - all below same as BaseEventPage
    protected VerticalLayout getMarginLayout()
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setWidth("25px");
        layout.setHeight(COL_MIN_HEIGHT + "px");

        return layout;
    }

    protected void setMenuLeftStyle(BaseOrg baseOrg)
    {
        String styleName = "menu-left-" + baseOrg.getTag();

        Page.Styles styles = Page.getCurrent().getStyles();
        String backgroundColor = getOrDefault(baseOrg.getNavBackgroundColor(), "whitesmoke");
        styles.add("." + styleName + " {background: " + backgroundColor + ";  border-right: 1px solid silver;}");

        leftWrapper.setStyleName(styleName);
    }
}