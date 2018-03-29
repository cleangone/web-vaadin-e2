package xyz.cleangone.e2.web.manager;

import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;

import javax.servlet.http.Cookie;

public class VaadinSessionManager
{
    private static String SESSION_MANAGER_SESSION_ATTR = "session.mgr";
    private static String IMAGE_MANAGER_SESSION_ATTR = "image.mgr";
    private static String USER_COOKIE_NAME = "user";

    public static SessionManager createSessionManager()
    {
        SessionManager sessionManager = getSessionManager();
        if (sessionManager == null)
        {
            sessionManager = new SessionManager();
            setSessionManager(sessionManager);
        }

        return sessionManager;
    }

    public static SessionManager getExpectedSessionManager()
    {
        SessionManager sessionManager = getSessionManager();
        if (sessionManager == null)
        {
            throw new IllegalStateException("Cannot get SessionManager from session");
        }

        return sessionManager;
    }

    public static void clearSessionManager()
    {
        setSessionManager(null);
    }

    private static SessionManager getSessionManager()
    {
        return (SessionManager)VaadinSession.getCurrent().getAttribute(SESSION_MANAGER_SESSION_ATTR);
    }

    private static void setSessionManager(SessionManager sessionManager)
    {
        VaadinSession.getCurrent().setAttribute(SESSION_MANAGER_SESSION_ATTR, sessionManager);
    }

    public static Cookie getUserCookie()
    {
        return getCookie(USER_COOKIE_NAME);
    }
    public static void setUserCookie(String token)
    {
        BrowserCookie.setCookie(USER_COOKIE_NAME, token);
    }
    public static void clearUserCookie()
    {
        BrowserCookie.setCookie(USER_COOKIE_NAME, "");
    }

    public static void removeUserCookie()
    {
        Cookie cookie = getUserCookie();

        if (cookie != null)
        {
            cookie.setValue(null);
            // By setting the cookie maxAge to 0 it will deleted immediately
            cookie.setMaxAge(0);
            cookie.setPath("/");
            VaadinService.getCurrentResponse().addCookie(cookie);
        }
    }

    private static Cookie getCookie(String name)
    {
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();

        if (cookies == null)
        {
            return null;
        }

        for (Cookie cookie : cookies)
        {
            if (name.equals(cookie.getName()))
            {
                return cookie;
            }
        }

        return null;
    }
}
