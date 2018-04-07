package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.stats;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.VerticalLayout;
import xyz.cleangone.data.manager.OrgManager;

public abstract class BaseStatsAdmin extends VerticalLayout
{
    protected String orgId;

    public BaseStatsAdmin()
    {
        setSizeFull();
        setMargin(new MarginInfo(true, false, true, false)); // T/R/B/L
        setSpacing(true);
    }

    public void set(OrgManager orgMgr)
    {
        orgId = orgMgr.getOrgId();
    }

    public abstract void set();
}