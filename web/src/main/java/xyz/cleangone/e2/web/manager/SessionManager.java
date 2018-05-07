package xyz.cleangone.e2.web.manager;

import com.vaadin.navigator.Navigator;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.item.CartItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.aws.dynamo.entity.person.UserToken;
import xyz.cleangone.data.aws.dynamo.entity.purchase.Cart;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.OrgPage;

import java.util.*;

/**
 * A session conststs of:
 * - a userManager if user has logged in
 * - an orgManager that controls the current org, which was set by direct login or the user
 *   selecting one of their orgs
 * - a cartManager that contains the cart for the current org
 */
public class SessionManager
{
    private UserManager userMgr = new UserManager();
    private OrgManager orgMgr = new OrgManager();
    private EventManager eventMgr = new EventManager();
    private Cart cart = new Cart(OrgPage.NAME);

    private String url;
    private boolean isMobileBrowser;
    private String initialOrgTag;
    private String navToAfterLogin;
    private String msg;
    private Set<String> eventViews = new HashSet<>();
    private Action currentAction;
    private ViewStatusCache viewStatusCache = new ViewStatusCache();

    public void reset()
    {
        userMgr.logout();
        resetOrg();
    }

    public String getInitialOrgTag()
    {
        return initialOrgTag;
    }
    public void setInitialOrgTag(String initialOrgTag)
    {
        this.initialOrgTag = initialOrgTag;
    }

    public String getUrl(String paramName, UserToken token)
    {
        return getUrl(paramName, token.getId());
    }
    public String getUrl(String paramName, String paramValue)
    {
        return url + "?" + paramName + "=" + paramValue;
    }
    public String getUrl()
    {
        return url;
    }
    public void setUrl(String url)
    {
        this.url = url;
    }

    public OrgManager getOrgManager()
    {
        return orgMgr;
    }
    public List<Organization> getOrgs()
    {
        return orgMgr.getAll();
    }
    public boolean hasOrg()
    {
        return (orgMgr.getOrg() != null);
    }
    public Organization getOrg()
    {
        return orgMgr.getOrg();
    }
    public String getOrgId()
    {
        return orgMgr.getOrg() == null ? null : orgMgr.getOrg().getId();
    }
    public void setOrg(Organization org)
    {
        orgMgr.setOrg(org);
    }
    public String getOrgName()
    {
        return hasOrg() ? orgMgr.getOrg().getName() : null;
    }

    public void createOrg(String name)
    {
        orgMgr.save(new Organization(name));
    }
    public void resetOrg()
    {
        orgMgr.setOrg(null);
    }


    //
    // Events
    //
    public EventManager getEventManager()
    {
        if (eventMgr.getOrg() == null) { resetEventManager(); }
        return eventMgr;
    }
    public void resetEventManager()
    {
        eventMgr.setOrg(orgMgr.getOrg());
    }
    public EventManager getResetEventManager()
    {
        resetEventManager();
        return getEventManager();
    }

    public ViewStatus getViewStatus(String eventId, String categoryId)
    {
        return viewStatusCache.getViewStatus(eventId, categoryId);
    }
    public ViewStatus getViewStatus()
    {
        return (eventMgr.getEvent() == null || eventMgr.getCategory() == null) ? new ViewStatus() :
            getViewStatus(eventMgr.getEvent().getId(), eventMgr.getCategory().getId());
    }

    //
    // Users
    //
    public UserManager getUserManager()
    {
        return userMgr;
    }
    public UserManager getPopulatedUserManager()
    {
        if (!hasUser()) { throw new IllegalStateException("User not set"); }
        return userMgr;
    }

    public User getUser() { return (userMgr.getUser()); }
    public boolean hasUser()
    {
        return (getUser() != null);
    }
    public boolean hasSuperUser()
    {
        return (hasUser() && getUser().isSuperAdmin());
    }

    public String getNavToAfterLogin(String defaultPage)
    {
        String navTo = navToAfterLogin == null ? defaultPage : navToAfterLogin;
        navToAfterLogin = null;
        return navTo;
    }
    public void setNavToAfterLogin(String navToAfterLogin)
    {
        this.navToAfterLogin = navToAfterLogin;
    }


    // todo - cheap work around for now
    public Action getCurrentAction()
    {
        return currentAction;
    }
    public void setCurrentAction(Action currentAction)
    {
        this.currentAction = currentAction;
    }


    public String getAndClearMsg()
    {
        String returnMsg = msg == null ? "" : msg;
        msg = null;
        return returnMsg;
    }

    public void resetEventViews()
    {
        eventViews.clear();
    }

    public void navigateTo(OrgEvent event, Navigator nav)
    {
        getEventManager().setEvent(event);

        // todo - weird error when tab w page deleted and then site revisited
        String viewName = EventPage.NAME + "-" + getOrg().getTag() + "-" + event.getTag();
        if (!eventViews.contains(viewName))
        {
            nav.addView(viewName, new EventPage());
            eventViews.add(viewName);
        }

        nav.navigateTo(viewName);
    }

    public String getMsg()
    {
        return msg;
    }
    public void setMsg(String msg)
    {
        this.msg = msg;
    }
    public void resetMsg()
    {
        msg = null;
    }

    public Cart getCart()
    {
        return cart;
    }
    public void resetCart()
    {
        cart.clear();
    }
    public void setCartReturnPage(String returnPage)
    {
        cart.setReturnPage(returnPage);
    }
    public void addCartItem(CartItem item)
    {
        cart.addItem(item);
    }

    public boolean isMobileBrowser()
    {
        return isMobileBrowser;
    }
    public void setIsMobileBrowser(boolean mobileBrowser)
    {
        isMobileBrowser = mobileBrowser;
    }
}