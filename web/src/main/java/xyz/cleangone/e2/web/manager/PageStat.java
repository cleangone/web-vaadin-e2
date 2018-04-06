package xyz.cleangone.e2.web.manager;

import xyz.cleangone.data.cache.EntityLastTouchedCache;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;

import java.math.BigDecimal;
import java.util.Date;

public class PageStat
{
    private final String pageName;
    private int totalCalls;

    private final RetrievalStats pageUnchanged = new RetrievalStats();
    private final RetrievalStats noRetrieval = new RetrievalStats();
    private final RetrievalStats retrieval = new RetrievalStats();

    public PageStat(String pageName)
    {
        this.pageName = pageName;
    }

    public void addRetrievalTime(PageDisplayType pageDisplayType, Date start)
    {
        totalCalls++;

        if (pageDisplayType == PageDisplayType.NoChange) { pageUnchanged.increment(start); }
        else if (pageDisplayType == PageDisplayType.NoRetrieval) { noRetrieval.increment(start); }
        else if (pageDisplayType == PageDisplayType.ObjectRetrieval) { retrieval.increment(start); }
    }

    public int getUnchangedPages()
    {
        return pageUnchanged.calls;
    }
    public String getUnchangedAvgSeconds()
    {
        return pageUnchanged.getAvgSeconds().toString();
    }

    public int getNoRetrievals()
    {
        return noRetrieval.calls;
    }
    public String getNoRetrievalAvgSeconds()
    {
        return noRetrieval.getAvgSeconds().toString();
    }

    public int getRetrievals()
    {
        return retrieval.calls;
    }
    public String getRetrievalAvgSeconds()
    {
        return retrieval.getAvgSeconds().toString();
    }

    public String getPageName()
    {
        return pageName;
    }
    public int getTotalCalls()
    {
        return totalCalls;
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

        BigDecimal getAvgSeconds()
        {
            if (calls == 0) { return BigDecimal.valueOf(0); }

            BigDecimal avgMillis = (new BigDecimal(totalTime)).divide(new BigDecimal(calls), 3, BigDecimal.ROUND_UP);
            return avgMillis.divide(new BigDecimal(1000), 3, BigDecimal.ROUND_UP);
        }
    }

}
