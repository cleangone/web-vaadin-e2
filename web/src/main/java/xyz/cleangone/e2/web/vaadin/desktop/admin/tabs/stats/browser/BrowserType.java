package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.stats.browser;

public enum BrowserType
{
    ALL("All"),
    COMPUTER("Computer"),
    MOBILE("All Mobile"),
    IOS("IOS"),
    ANDRIOD("Android"),
    WINDOWS_PHONE("Windows Phone");

    private final String text;
    BrowserType(final String text)
    {
        this.text = text;
    }
    public String toString()
    {
        return text;
    }
}
