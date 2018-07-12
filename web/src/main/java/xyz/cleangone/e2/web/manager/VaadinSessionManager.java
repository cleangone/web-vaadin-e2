package xyz.cleangone.e2.web.manager;

import com.vaadin.server.VaadinSession;

public class VaadinSessionManager
{
    private static String SESSION_MANAGER_SESSION_ATTR = "session.mgr";

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
}
