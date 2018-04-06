package xyz.cleangone.e2.web.vaadin.desktop.admin.event;

public enum EventAdminPageType
{
    EVENTS("All Events"),
    GENERAL("General"),
    PARTICIPANTS("Participants"),
    TAGS("Tags"),
    ITEMS("Items"),
    DATES("Dates"),
    ROLES("User Roles"),
    USERS("Users"),
    DONATIONS("Donations"),
    PURCHASES("Purchases");

    private final String text;

    EventAdminPageType(final String text)
    {
        this.text = text;
    }

    public String toString()
    {
        return text;
    }
}

