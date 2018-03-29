package xyz.cleangone.e2.web.vaadin.desktop.banner;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.purchase.Cart;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.manager.VaadinSessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.AdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.admin.SuperAdminPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.*;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.payment.PaymentPage;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class ActionBar extends HorizontalLayout implements MessageDisplayer
{
    private static String STYLE_NAME = "actionBarMain";
    private static String DEFAULT_BACKGROUND_COLOR = "whitesmoke";

    private MenuBar leftMenuBar = new MenuBar();
    private MenuBar centerMenuBar = new MenuBar();
    private MenuBar rightMenuBar = new MenuBar();
    private MenuBar.MenuItem msgMenuItem;
    private MenuBar.MenuItem cartMenuItem;

    public ActionBar()
    {
        // todo - make use of LeftCenterRightLayout
        setWidth("100%");
        setMargin(false);
        setSpacing(false);
        setStyleName(STYLE_NAME);

        HorizontalLayout leftLayout = new HorizontalLayout();
        leftLayout.setWidth("10%");
        leftMenuBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
        leftLayout.addComponent(leftMenuBar);

        HorizontalLayout centerLayout = new HorizontalLayout();
        centerLayout.setWidth("50");
        centerMenuBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
        centerLayout.addComponents(centerMenuBar);

        HorizontalLayout rightLayout = new HorizontalLayout();
        rightLayout.setWidth("40%");
        rightMenuBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
        rightLayout.addComponents(rightMenuBar, getHtmlLabel(""));

        addComponents(leftLayout, centerLayout, rightLayout);
        setComponentAlignment(rightLayout, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));
    }

    public void reset(SessionManager sessionMgr)
    {
        requireNonNull(sessionMgr);

        leftMenuBar.removeItems();
        centerMenuBar.removeItems();
        rightMenuBar.removeItems();

        // left, center menus
        if (sessionMgr.hasSuperUser())
        {
            addNavigateItem(SuperAdminPage.NAME, leftMenuBar);
        }
        if (sessionMgr.hasOrg())
        {
            if (sessionMgr.hasUser())
            {
                UserManager userMgr = sessionMgr.getUserManager();
                OrgManager orgMgr = sessionMgr.getOrgManager();
                TagManager tagMgr = orgMgr.getTagManager();

                if (userMgr.userIsAdmin(orgMgr.getOrg()) ||
                    userMgr.userHasEventAdmin(orgMgr.getOrg(), tagMgr.getEventAdminRoleTagIds()))
                {
                    addNavigateItem(AdminPage.NAME, leftMenuBar);
                }
            }

            MenuBar.MenuItem homeItem = centerMenuBar.addItem("", null, getNavigateCmd(OrgPage.NAME));
            setMenuItem(homeItem, VaadinIcons.HOME, "Home");

            EventManager eventMgr = sessionMgr.getEventManager();

            OrgEvent currEvent = eventMgr.getEvent();
            if (currEvent != null)
            {
                String styleName = addActionBarStyle(currEvent);
                setStyleName(styleName);
            }

            List<OrgEvent> events = eventMgr.getActiveEvents();
            if (!events.isEmpty())
            {
                MenuBar.MenuItem eventsItem = centerMenuBar.addItem("Events", null, null);
                for (OrgEvent event : events)
                {
                    if (!event.getUseOrgBanner())
                    {
                        eventsItem.addItem(event.getName(), null, new MenuBar.Command() {
                            public void menuSelected(MenuBar.MenuItem selectedItem) {
                                sessionMgr.navigateTo(event, getUI().getNavigator());
                            }
                        });
                    }
                }
            }

            addNavigateItem(CalendarPage.NAME, VaadinIcons.CALENDAR, centerMenuBar);
            msgMenuItem = centerMenuBar.addItem(sessionMgr.getAndClearMsg(), null, null);
        }

        // right menu
        cartMenuItem = rightMenuBar.addItem(" ", VaadinIcons.CART, getNavigateCmd(CartPage.NAME));
        setCartMenuItem(sessionMgr);

        UserManager userMgr = sessionMgr.getUserManager();
        if (userMgr.hasUser())
        {
            MenuBar.MenuItem profileItem = rightMenuBar.addItem(" " + userMgr.getPersonFirstName(), null, null);
            profileItem.setIcon(VaadinIcons.USER);
            profileItem.setDescription(ProfilePage.DISPLAY_NAME);
            profileItem.addItem(ProfilePage.NAME, null, getNavigateCmd(ProfilePage.NAME));

            MenuBar.Command logoutCmd = new MenuBar.Command() {
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    userMgr.logout();
                    VaadinSessionManager.clearUserCookie();
                    navigateTo("");
                }
            };

            profileItem.addItem("Logout", null, logoutCmd);
        }
        else
        {
            MenuBar.MenuItem signinItem = rightMenuBar.addItem(SigninPage.DISPLAY_NAME, null, null);
            signinItem.addItem("Login", null, getNavigateCmd(SigninPage.NAME));
            signinItem.addItem(CreateAccountPage.DISPLAY_NAME, null, getNavigateCmd(CreateAccountPage.NAME));
            signinItem.addItem("Reset Password", null, getNavigateCmd(PasswordRequestPage.NAME));
        }
    }

    private void addNavigateItem(String pageName, Resource icon, MenuBar menuBar)
    {
        MenuBar.MenuItem menuItem = menuBar.addItem("", null, getNavigateCmd(pageName));
        setMenuItem(menuItem, icon, pageName);
    }

    private void setMenuItem(MenuBar.MenuItem menuItem, Resource icon, String description)
    {
        menuItem.setIcon(icon);
        menuItem.setStyleName("icon-only");
        menuItem.setDescription(description);
    }

    private void addNavigateItem(String pageName, String displayName, MenuBar menuBar)
    {
        menuBar.addItem(displayName, null, getNavigateCmd(pageName));
    }

    private void addNavigateItem(String pageName, MenuBar menuBar)
    {
        menuBar.addItem(pageName, null, getNavigateCmd(pageName));
    }

    private MenuBar.Command getNavigateCmd(String pageName)
    {
        return new MenuBar.Command() {
            public void menuSelected(MenuBar.MenuItem selectedItem) { navigateTo(pageName); }
        };
    }

    private void navigateTo(String pageName) { getUI().getNavigator().navigateTo(pageName); }

    public void setCartMenuItem(SessionManager sessionMgr)
    {
        Cart cart = sessionMgr.getCart();
        cartMenuItem.setText(cart.isEmpty() ? " " : "(" + cart.getItems().size() + ")");
        cartMenuItem.setIcon(cart.isEmpty() ? VaadinIcons.CART_O : VaadinIcons.CART);
    }

    public void displayMessage(String msg)
    {
        msgMenuItem.setText(msg);
    }

    private String addActionBarStyle(OrgEvent event)
    {
        if (event.getBarBackgroundColor() == null) { return STYLE_NAME; }

        String styleName = STYLE_NAME + "-" + event.getTag();
        addActionBarStyle(styleName, event.getBarBackgroundColor());

        return styleName;
    }

    public MenuBar getLeftMenuBar()
    {
        return leftMenuBar;
    }
    public MenuBar getCenterMenuBar()
    {
        return centerMenuBar;
    }
    public MenuBar getRightMenuBar()
    {
        return rightMenuBar;
    }

    public static void addActionBarStyle()
    {
        addActionBarStyle(STYLE_NAME, DEFAULT_BACKGROUND_COLOR);
    }

    public static void addActionBarStyle(String styleName, String backgroundColor)
    {
        Page.Styles styles = Page.getCurrent().getStyles();
        styles.add("." + styleName +
            " { background: " + backgroundColor + "; border-top: 1px solid silver; border-bottom: 1px solid silver; }");
    }

}
