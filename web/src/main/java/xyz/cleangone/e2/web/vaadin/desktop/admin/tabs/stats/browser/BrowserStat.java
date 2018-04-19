package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.stats.browser;

import com.vaadin.server.Page;

public class BrowserStat
{
    private BrowserType browserType;
    private int hits;
    private int minWindowWidth;
    private int maxWindowWidth;

    public BrowserStat(BrowserType browserType)
    {
        this.browserType = browserType;
    }

    public void addPage(Page page)
    {
        hits++;

        int width = page.getBrowserWindowWidth();
        minWindowWidth = (minWindowWidth == 0 ? width : Math.min(width, minWindowWidth));
        maxWindowWidth = (maxWindowWidth == 0 ? width : Math.max(width, maxWindowWidth));
    }

    public BrowserType getBrowserType()
    {
        return browserType;
    }
    public int getHits()
    {
        return hits;
    }
    public int getMinWindowWidth()
    {
        return minWindowWidth;
    }
    public int getMaxWindowWidth()
    {
        return maxWindowWidth;
    }
}