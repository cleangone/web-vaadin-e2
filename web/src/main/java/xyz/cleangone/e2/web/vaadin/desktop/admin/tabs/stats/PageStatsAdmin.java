package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.stats;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.Grid;
import com.vaadin.ui.components.grid.HeaderRow;
import xyz.cleangone.data.cache.EntityCacheStat;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.e2.web.manager.PageStat;
import xyz.cleangone.e2.web.manager.PageStats;

import java.util.ArrayList;
import java.util.List;

public class PageStatsAdmin extends BaseStatsAdmin
{
    private List<PageStat> pageStats = new ArrayList<>();

    public PageStatsAdmin()
    {
        Grid<PageStat> grid = getStatsGrid();
        addComponent(grid);
        setExpandRatio(grid, 1.0f);
    }

    public void set()
    {
        pageStats.clear();
        pageStats.addAll(PageStats.getPageStats(orgId));
    }

    private Grid<PageStat> getStatsGrid()
    {
        Grid<PageStat> grid = new Grid<>();
        grid.setWidth("100%");
        grid.setHeight("100%");

        HeaderRow topHeader = grid.prependHeaderRow();
        grid.addColumn(PageStat::getPageName).setCaption("Page");
        addCols(grid, topHeader, "All",       PageStat::getAllPages, PageStat::getAllAvgSeconds);
        addCols(grid, topHeader, "Unchanged", PageStat::getUnchangedPages, PageStat::getUnchangedAvgSeconds);
        addCols(grid, topHeader, "Retrieved", PageStat::getRetrievals, PageStat::getRetrievalAvgSeconds);

        grid.setDataProvider(new ListDataProvider<>(pageStats));

        return grid;
    }

    private void addCols(Grid<PageStat> grid, HeaderRow headerRow, String desc,
        ValueProvider<PageStat, Integer> hitsValueProvider, ValueProvider<PageStat, String> avgValueProvider)
    {
        Grid.Column hits = grid.addColumn(hitsValueProvider).setCaption("Hits");
        Grid.Column avg  = grid.addColumn(avgValueProvider).setCaption("Avg Disp (Secs)");
        headerRow.join(hits, avg).setText(desc + " Pages");
    }
}