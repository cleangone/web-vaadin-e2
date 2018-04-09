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

import java.util.ArrayList;
import java.util.List;

public class CacheStatsAdmin extends BaseStatsAdmin
{
    private List<EntityCacheStat> cacheStats = new ArrayList<>();

    public CacheStatsAdmin()
    {
        Grid<EntityCacheStat> grid = getStatsGrid();
        addComponent(grid);
        setExpandRatio(grid, 1.0f);
    }

    public void set()
    {
        cacheStats.clear();
        cacheStats.addAll(EventManager.EVENT_CACHE.getCacheStats(orgId));
        cacheStats.addAll(OrgManager.PERSON_CACHE.getCacheStats(orgId));
        cacheStats.addAll(TagManager.TAG_CACHE.getCacheStats(orgId));
        cacheStats.addAll(EventManager.EVENT_DATE_CACHE.getCacheStats(orgId));
        cacheStats.addAll(EventManager.PARTICIPANT_CACHE.getCacheStats(orgId));
    }

    private Grid<EntityCacheStat> getStatsGrid()
    {
        Grid<EntityCacheStat> grid = new Grid<>();
        grid.setWidth("100%");
        grid.setHeight("100%");

        HeaderRow topHeader = grid.prependHeaderRow();
        grid.addColumn(EntityCacheStat::getEntityName).setCaption("Entity");
        grid.addColumn(EntityCacheStat::getEntityType).setCaption("Entity Type");
        addCols(grid, topHeader, "Hits",   EntityCacheStat::getHits, EntityCacheStat::getAvgHitSeconds);
        addCols(grid, topHeader, "Misses", EntityCacheStat::getMisses, EntityCacheStat::getAvgMissSeconds);

        grid.setDataProvider(new ListDataProvider<>(cacheStats));

        return grid;
    }

    private void addCols(Grid<EntityCacheStat> grid, HeaderRow headerRow, String desc,
        ValueProvider<EntityCacheStat, Integer> hitsValueProvider, ValueProvider<EntityCacheStat, String> avgValueProvider)
    {
        Grid.Column count = grid.addColumn(hitsValueProvider).setCaption(desc);
        Grid.Column avg   = grid.addColumn(avgValueProvider).setCaption("Avg Disp (Secs)");
        headerRow.join(count, avg).setText(desc);
    }
}