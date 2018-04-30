package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import com.vaadin.ui.*;

public class BidsPage extends BaseProfilePage
{
    public static final String NAME = "Bids";
    public static final String WATCH_NAME = "Watched";
    public static final String WATCH_DISPLAY_NAME = "Watched Items";

    public BidsPage(ProfilePageType currPageType)
    {
        super(currPageType);

        components.put(ProfilePageType.BIDS, new BidsAdmin(actionBar));
        components.put(ProfilePageType.WATCHED, new WatchLayout(this, actionBar));
        components.put(ProfilePageType.BID_HISTORY, new ActionsAdmin(actionBar, ProfilePageType.BIDS));
    }

    protected Component getLinksLayout()
    {
        return getLinksLayout(ProfilePageType.BIDS, ProfilePageType.WATCHED, ProfilePageType.BID_HISTORY);
    }
}