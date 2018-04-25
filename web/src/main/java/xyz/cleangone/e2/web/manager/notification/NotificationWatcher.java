package xyz.cleangone.e2.web.manager.notification;

import java.util.HashMap;
import java.util.Map;

public class NotificationWatcher
{
    private static final Map<String, NotificationScheduler> ORG_ID_TO_SCHEDULER = new HashMap<>();

    public static void startWatch(String orgId)
    {
        NotificationScheduler scheduler = NotificationWatcher.getScheduler(orgId);
        scheduler.schedule();
    }

    private static NotificationScheduler getScheduler(String orgId)
    {
        NotificationScheduler scheduler = ORG_ID_TO_SCHEDULER.get(orgId);
        if (scheduler == null)
        {
            scheduler = new NotificationScheduler(orgId);
            ORG_ID_TO_SCHEDULER.put(orgId, scheduler);
        }

        return scheduler;
    }
}
