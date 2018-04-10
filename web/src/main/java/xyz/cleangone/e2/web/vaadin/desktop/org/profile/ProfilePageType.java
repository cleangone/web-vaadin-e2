package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

public enum ProfilePageType
{
    GENERAL("General"),
    DONATIONS("Donations"),
    PURCHASES("Purchases"),
    BIDS("Bids");

    private final String text;

    ProfilePageType(final String text)
    {
        this.text = text;
    }

    public String toString()
    {
        return text;
    }
}

