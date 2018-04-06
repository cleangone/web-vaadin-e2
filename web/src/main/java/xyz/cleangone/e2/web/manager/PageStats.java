package xyz.cleangone.e2.web.manager;

import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import java.util.*;

public class PageStats
{
    private static final Map<String, Map<String, PageStat>> ORG_ID_TO_STATS = new HashMap<>();

    public static void addRetrievalTime(String orgId, String pageName, PageDisplayType pageDisplayType, Date start)
    {
        if (pageName == null || pageDisplayType == PageDisplayType.NotApplicable) { return; }

        Map<String, PageStat> pageNameToStats = ORG_ID_TO_STATS.get(orgId);
        if (pageNameToStats == null)
        {
            pageNameToStats = new HashMap<>();
            ORG_ID_TO_STATS.put(orgId, pageNameToStats);
        }

        PageStat pageStat = pageNameToStats.get(pageName);
        if (pageStat == null)
        {
            pageStat = new PageStat(pageName);
            pageNameToStats.put(pageName, pageStat);
        }

        pageStat.addRetrievalTime(pageDisplayType, start);
    }

    public static Collection<PageStat> getPageStats(String orgId)
    {
        Map<String, PageStat> pageNameToStats = ORG_ID_TO_STATS.get(orgId);
        if (pageNameToStats == null) { return new ArrayList<PageStat>(); }

        return pageNameToStats.values();
    }
}
