package xyz.cleangone.e2.web.manager;

import xyz.cleangone.data.cache.EntityLastTouchedCache;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;

import java.math.BigDecimal;
import java.util.Date;

public class PageStat
{
    private final String pageName;

    private final RetrievalStats all = new RetrievalStats();
    private final RetrievalStats pageUnchanged = new RetrievalStats();
    private final RetrievalStats retrieval = new RetrievalStats();

    public PageStat(String pageName)
    {
        this.pageName = pageName;
    }

    public void addRetrievalTime(PageDisplayType pageDisplayType, Date start)
    {
        all.increment(start);

        if (pageDisplayType == PageDisplayType.NoChange) { pageUnchanged.increment(start); }
        else if (pageDisplayType == PageDisplayType.ObjectRetrieval) { retrieval.increment(start); }
    }

    public int getAllPages()
    {
        return all.calls;
    }
    public String getAllAvgSeconds()
    {
        return all.getAvgSeconds();
    }

    public int getUnchangedPages()
    {
        return pageUnchanged.calls;
    }
    public String getUnchangedAvgSeconds()
    {
        return pageUnchanged.getAvgSeconds();
    }

    public int getRetrievals()
    {
        return retrieval.calls;
    }
    public String getRetrievalAvgSeconds()
    {
        return retrieval.getAvgSeconds();
    }

    public String getPageName()
    {
        return pageName;
    }

    class RetrievalStats
    {
        int calls;
        long totalTime;

        void increment(Date start)
        {
            Date now = new Date();
            calls++;
            long callTime = now.getTime() - start.getTime();
            totalTime += callTime;
        }

        String getAvgSeconds()
        {
            if (calls == 0) { return "0"; }

            BigDecimal avgMillis = (new BigDecimal(totalTime)).divide(new BigDecimal(calls), 3, BigDecimal.ROUND_UP);
            return avgMillis.divide(new BigDecimal(1000), 3, BigDecimal.ROUND_UP).toString();
        }
    }

}
