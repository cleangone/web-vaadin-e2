package xyz.cleangone.e2.web.vaadin.desktop.org;

import com.vaadin.navigator.View;
import xyz.cleangone.e2.web.manager.SessionManager;

public abstract class BaseOrgPage extends BasePage implements View
{
    public BaseOrgPage()
    {
        super(BannerStyle.Single);
    }

    protected PageDisplayType set(SessionManager sessionMgr)
    {
        super.set(sessionMgr);

        resetHeader();
        return set();
    }

    protected abstract PageDisplayType set();
}