package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import com.vaadin.ui.*;

public class BidsPage extends BaseProfilePage
{
    public static final String NAME = "Bids";

    public BidsPage()
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