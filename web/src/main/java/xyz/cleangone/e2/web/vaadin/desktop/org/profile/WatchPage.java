package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import com.vaadin.ui.Component;

public class WatchPage extends BaseProfilePage
{
    public static final String NAME = "Watch";
    public static final String DISPLAY_NAME = "Watched items";

    public WatchPage()
    {
        super(ProfilePageType.BIDS);

        components.put(ProfilePageType.BIDS, new BidsAdmin(actionBar));
        components.put(ProfilePageType.BID_HISTORY, new ActionsAdmin(actionBar, ProfilePageType.BIDS));
    }

    protected Component getLinksLayout()
    {
        return getLinksLayout(ProfilePageType.BIDS, ProfilePageType.BID_HISTORY);
    }
}