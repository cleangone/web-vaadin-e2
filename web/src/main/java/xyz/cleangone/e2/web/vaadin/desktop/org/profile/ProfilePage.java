package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import com.vaadin.ui.*;

public class ProfilePage extends BaseProfilePage
{
    public static final String NAME = "UserProfile";
    public static final String DISPLAY_NAME = "User Profile";

    public ProfilePage()
    {
        super(ProfilePageType.GENERAL);

        components.put(ProfilePageType.GENERAL,     new ProfileAdmin(actionBar));
        components.put(ProfilePageType.DONATIONS,   new ActionsAdmin(actionBar, ProfilePageType.DONATIONS));
        components.put(ProfilePageType.PURCHASES,   new ActionsAdmin(actionBar, ProfilePageType.PURCHASES));
        components.put(ProfilePageType.BID_HISTORY, new ActionsAdmin(actionBar, ProfilePageType.BIDS));
    }

    protected Component getLinksLayout()
    {
        addStyles();

        VerticalLayout linkLayout = new VerticalLayout();
        linkLayout.setMargin(false);
        linkLayout.setSpacing(false);

        linkLayout.addComponent(getLink(ProfilePageType.GENERAL));
        linkLayout.addComponent(getLink(ProfilePageType.DONATIONS));
        linkLayout.addComponent(getLink(ProfilePageType.PURCHASES));
        linkLayout.addComponent(getLink(ProfilePageType.BID_HISTORY));

        Label label = new Label("User Profile");
        label.setStyleName(STYLE_FONT_BOLD);

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.addComponents(label, linkLayout);

        return layout;
    }
}