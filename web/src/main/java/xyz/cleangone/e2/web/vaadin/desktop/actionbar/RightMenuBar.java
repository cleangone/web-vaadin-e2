package xyz.cleangone.e2.web.vaadin.desktop.actionbar;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.MenuBar;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.aws.dynamo.entity.purchase.Cart;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.manager.VaadinSessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.*;
import xyz.cleangone.e2.web.vaadin.desktop.org.profile.ProfilePage;

import static xyz.cleangone.e2.web.vaadin.util.PageUtils.*;

public class RightMenuBar extends BaseMenuBar
{
    private MenuBar.MenuItem cartMenuItem;

    public PageDisplayType set(SessionManager sessionMgr)
    {
        super.set(sessionMgr);
        return set();
    }

    private PageDisplayType set()
    {
        User user = userMgr.getUser();

        if (changeManager.unchanged(user) &&
            changeManager.unchanged(user, EntityType.Entity) &&
            (user == null  || changeManager.unchanged(user.getPersonId(), EntityType.Entity)))
        {
            setCartMenuItem();
            return PageDisplayType.NoChange;
        }

        changeManager.reset(user);
        removeItems();

        cartMenuItem = addItem(" ", VaadinIcons.CART, getNavigateCmd(CartPage.NAME));
        setCartMenuItem();

        if (userMgr.hasUser())
        {
            MenuBar.MenuItem profileItem = addItem(" " + userMgr.getPersonFirstName(), null, null);
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
            MenuBar.MenuItem signinItem = addItem(SigninPage.DISPLAY_NAME, null, null);
            signinItem.addItem("Login", null, getNavigateCmd(SigninPage.NAME));
            signinItem.addItem(CreateAccountPage.DISPLAY_NAME, null, getNavigateCmd(CreateAccountPage.NAME));
            signinItem.addItem("Reset Password", null, getNavigateCmd(PasswordRequestPage.NAME));
        }

        return PageDisplayType.NoRetrieval;
    }

    public void setCartMenuItem()
    {
        Cart cart = sessionMgr.getCart();

        if(cartMenuItem != null)
        {
            cartMenuItem.setText(cart.isEmpty() ? " " : "(" + cart.getItems().size() + ")");
            cartMenuItem.setIcon(cart.isEmpty() ? VaadinIcons.CART_O : VaadinIcons.CART);
        }
    }

}
