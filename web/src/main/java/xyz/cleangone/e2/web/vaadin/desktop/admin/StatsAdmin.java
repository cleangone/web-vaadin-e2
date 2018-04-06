package xyz.cleangone.e2.web.vaadin.desktop.admin;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.cache.EntityCacheStat;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;

import java.util.*;

public class StatsAdmin extends VerticalLayout
{
    private List<EntityCacheStat> cacheStats = new ArrayList<>();

    public StatsAdmin()
    {
        setSizeFull();
        setMargin(new MarginInfo(true, false, true, false)); // T/R/B/L
        setSpacing(true);

        Grid<EntityCacheStat> grid = getStatsGrid();
        addComponent(grid);
        setExpandRatio(grid, 1.0f);
    }

    public void set(OrgManager orgMgr)
    {
        String orgId = orgMgr.getOrgId();

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

        grid.addColumn(EntityCacheStat::getEntityName).setCaption("Entity");
        grid.addColumn(EntityCacheStat::getEntityType).setCaption("Entity Type");
        grid.addColumn(EntityCacheStat::getHits).setCaption("Cache Hits");
        grid.addColumn(EntityCacheStat::getAvgHitSeconds).setCaption("Avg Retrieval Seconds (Hits)");
        grid.addColumn(EntityCacheStat::getMisses).setCaption("Cache Misses");
        grid.addColumn(EntityCacheStat::getAvgMissSeconds).setCaption("Avg Retrieval Seconds (Misses)");

        grid.setDataProvider(new ListDataProvider<>(cacheStats));

        return grid;
    }
}