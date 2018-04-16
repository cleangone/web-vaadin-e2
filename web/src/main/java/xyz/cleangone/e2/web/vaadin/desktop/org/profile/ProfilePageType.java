package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

public enum ProfilePageType
{
    GENERAL("General"),
    BIDS("Bids"),
    DONATIONS("Donations"),
    PURCHASES("Purchases"),
    BID_HISTORY("Bid History");

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

