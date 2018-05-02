package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import com.vaadin.ui.*;

public class BidsPage extends BaseProfilePage
{
    public static final String NAME = "Bids";
    public static final String WATCH_NAME = "Watched";
    public static final String WATCH_DISPLAY_NAME = "Watched Items";

    private WatchLayout watchLayout;

    public BidsPage(ProfilePageType currPageType)
    {
        super(currPageType);

        watchLayout = new WatchLayout(this, UI.getCurrent().getPage().getBrowserWindowWidth(), actionBar);
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> resetPageWidth());

        components.put(ProfilePageType.BIDS, new BidsAdmin(actionBar));
        components.put(ProfilePageType.WATCHED, watchLayout);
        components.put(ProfilePageType.BID_HISTORY, new ActionsAdmin(actionBar, ProfilePageType.BIDS));
    }

    protected Component getLinksLayout()
    {
        return getLinksLayout(ProfilePageType.BIDS, ProfilePageType.WATCHED, ProfilePageType.BID_HISTORY);
    }

    private void resetPageWidth()
    {
        watchLayout.resetPageWidth(UI.getCurrent().getPage().getBrowserWindowWidth());
    }
}