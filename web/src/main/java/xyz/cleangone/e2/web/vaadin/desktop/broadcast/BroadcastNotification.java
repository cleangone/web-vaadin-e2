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

    public boolean hasItemId(String itemId)
    {
        return this.itemId.equals(itemId);
    }

    public String getOrgId()
    {
        return orgId;
    }
}
