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
        addStyles();

        VerticalLayout linkLayout = new VerticalLayout();
        linkLayout.setMargin(false);
        linkLayout.setSpacing(false);

        linkLayout.addComponent(getLink(ProfilePageType.BIDS));
        linkLayout.addComponent(getLink(ProfilePageType.BID_HISTORY));

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.addComponents(linkLayout);

        return layout;
    }

}