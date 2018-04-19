package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.stats;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.Grid;
import xyz.cleangone.e2.web.vaadin.desktop.MyUI;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.stats.browser.BrowserStat;

import java.util.ArrayList;
import java.util.List;


public class BrowserStatsAdmin extends BaseStatsAdmin
{
    private List<BrowserStat> browserStats = new ArrayList<>();

    public BrowserStatsAdmin()
    {
        Grid<BrowserStat> grid = getStatsGrid();
        addComponent(grid);
        setExpandRatio(grid, 1.0f);
    }

    public void set()
    {
        browserStats.clear();
        browserStats.addAll(MyUI.BROWSER_STATS.getStats());
    }

    private Grid<BrowserStat> getStatsGrid()
    {
        Grid<BrowserStat> grid = new Grid<>();
        grid.setWidth("100%");
        grid.setHeight("100%");

        grid.addColumn(BrowserStat::getBrowserType).setCaption("Browser Type");
        grid.addColumn(BrowserStat::getHits).setCaption("Hits");
        grid.addColumn(BrowserStat::getMinWindowWidth).setCaption("Min Window Width");
        grid.addColumn(BrowserStat::getMaxWindowWidth).setCaption("Max Window Width");

        grid.setDataProvider(new ListDataProvider<>(browserStats));

        return grid;
    }
}