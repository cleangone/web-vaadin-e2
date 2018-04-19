package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.stats;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.AdminPageType;

import java.util.*;

public class StatsAdmin extends HorizontalLayout
{
    private final NavCol navCol;
    private final Map<AdminPageType, BaseStatsAdmin> statComponents = new HashMap<>();
    private final VerticalLayout mainLayout = new VerticalLayout();

    public StatsAdmin()
    {
        navCol = new NavCol(this);
        statComponents.put(StatsAdminPageType.CACHE, new CacheStatsAdmin());
        statComponents.put(StatsAdminPageType.PAGE, new PageStatsAdmin());
        statComponents.put(StatsAdminPageType.BROWSER, new BrowserStatsAdmin());

        mainLayout.setMargin(new MarginInfo(false, true, false, false)); // T/R/B/L margins

        setSizeFull();
        setMargin(false);
        setSpacing(true);

        addComponents(navCol, mainLayout);
        setExpandRatio(mainLayout, 1.0f);
    }

    public void set(OrgManager orgMgr)
    {
        navCol.set();
        for (BaseStatsAdmin statComponent : statComponents.values())
        {
            statComponent.set(orgMgr);
        }

        setPage(StatsAdminPageType.CACHE);
    }

    public void setPage(AdminPageType pageType)
    {
        navCol.setLinks(pageType);
        
        BaseStatsAdmin component = statComponents.get(pageType);
        component.set();
        mainLayout.removeAllComponents();
        mainLayout.addComponent(component);
    }
}