package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.stats;

import com.vaadin.ui.VerticalLayout;
import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.AdminPageType;
import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.BaseNavCol;

import static xyz.cleangone.web.vaadin.util.VaadinUtils.MARGIN_FALSE;
import static xyz.cleangone.web.vaadin.util.VaadinUtils.SPACING_FALSE;
import static xyz.cleangone.web.vaadin.util.VaadinUtils.vertical;

public class NavCol extends BaseNavCol
{
    private final StatsAdmin statsAdmin;

    public NavCol(StatsAdmin statsAdmin)
    {
        this.statsAdmin = statsAdmin;
    }

    protected void addLinks()
    {
        VerticalLayout layout = vertical(MARGIN_FALSE, SPACING_FALSE);
        layout.addComponents(
            getLink(StatsAdminPageType.CACHE),
            getLink(StatsAdminPageType.PAGE),
            getLink(StatsAdminPageType.BROWSER));

        addComponent(layout);
        addSpacer(15);
    }

    protected void setPage(AdminPageType pageType)
    {
        statsAdmin.setPage(pageType);
    }
}
