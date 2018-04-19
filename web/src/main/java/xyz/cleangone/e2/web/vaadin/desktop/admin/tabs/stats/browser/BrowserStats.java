package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.stats.browser;

import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrowserStats
{
    private Map<BrowserType, BrowserStat> browserTypeToStat = new HashMap<>();
    private List<BrowserStat> browserStats = new ArrayList<>();

    BrowserStat allBrowers;
    BrowserStat mobileBrowers;

    public BrowserStats()
    {
        allBrowers = init(BrowserType.ALL);
        init(BrowserType.COMPUTER);
        mobileBrowers = init(BrowserType.MOBILE);
        init(BrowserType.IOS);
        init(BrowserType.ANDRIOD);
        init(BrowserType.WINDOWS_PHONE);
    }

    private BrowserStat init(BrowserType browserType)
    {
        BrowserStat browserStat = new BrowserStat(browserType);
        browserTypeToStat.put(browserType, browserStat);
        browserStats.add(browserStat);
        return browserStat;
    }

    public void addPage(Page page)
    {
        BrowserType browserType = getBrowserType(page.getWebBrowser());
        BrowserStat browserStat = browserTypeToStat.get(browserType);

        browserStat.addPage(page);
        allBrowers.addPage(page);
        if (browserStat.getBrowserType() == BrowserType.IOS ||
            browserStat.getBrowserType() == BrowserType.ANDRIOD ||
            browserStat.getBrowserType() == BrowserType.WINDOWS_PHONE)
        {
            mobileBrowers.addPage(page);
        }
    }

    private BrowserType getBrowserType(WebBrowser browser)
    {
        if (browser.isIOS()) { return BrowserType.IOS; }
        else if (browser.isAndroid()) {return BrowserType.ANDRIOD; }
        else if (browser.isWindowsPhone()) { return BrowserType.WINDOWS_PHONE; }
        else { return BrowserType.COMPUTER; }
    }

    public List<BrowserStat> getStats()
    {
        return browserStats;
    }
}