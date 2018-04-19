package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.stats;

import com.vaadin.ui.VerticalLayout;
import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.AdminPageType;
import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.BaseNavCol;

public class NavCol extends BaseNavCol
{
    private final StatsAdmin statsAdmin;

    public NavCol(StatsAdmin statsAdmin)
    {
        this.statsAdmin = statsAdmin;
    }

    protected void addLinks()
    {
        VerticalLayout layout = getTightLayout();
        layout.addComponent(getLink(StatsAdminPageType.CACHE));
        layout.addComponent(getLink(StatsAdminPageType.PAGE));
        layout.addComponent(getLink(StatsAdminPageType.BROWSER));

        addComponent(layout);
        addSpacer(15);
    }

    protected void setPage(AdminPageType pageType)
    {
        statsAdmin.setPage(pageType);
    }
}
