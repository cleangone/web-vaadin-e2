package xyz.cleangone.e2.web.manager;

import com.vaadin.server.VaadinService;
import xyz.cleangone.e2.web.manager.BrowserCookie;

import javax.servlet.http.Cookie;

public class CookieManager
{
    private static String USER_COOKIE_NAME = "user";

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
