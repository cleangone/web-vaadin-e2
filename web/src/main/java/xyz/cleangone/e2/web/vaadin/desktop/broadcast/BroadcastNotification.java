package xyz.cleangone.e2.web.vaadin.desktop.broadcast;

import static java.util.Objects.*;

public class BroadcastNotification
{
    private final String orgId;
    private final String itemId;

    public BroadcastNotification(String orgId, String itemId)
    {
        this.orgId = requireNonNull(orgId);
        this.itemId = requireNonNull(itemId);
    }

    public String getOrgId()
    {
        return orgId;
    }
    public String getItemId()
    {
        return itemId;
    }
}
