package xyz.cleangone.e2.web.manager.notification;

import xyz.cleangone.data.aws.dynamo.entity.notification.QueuedNotification;
import xyz.cleangone.data.manager.NotificationManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.*;

public class NotificationScheduler
{
    private static SimpleDateFormat SDF = new SimpleDateFormat("EEE MMM d, hh:mm:ss aaa");

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private NotificationManager notificationMgr;
    private QueuedNotification lastNotificationScheduled;

    public NotificationScheduler(String orgId)
    {
        notificationMgr = new NotificationManager(orgId);
    }

    public void schedule()
    {
        QueuedNotification notification = notificationMgr.getEarliestNotification();
        if (notification == null)
        {
            System.out.println("No notifications to schedule for orgId " + notificationMgr.getOrgId());
            return;
        }

        if (lastNotificationScheduled != null &&
            notification.getId().equals(lastNotificationScheduled.getId()) &&
            notification.getNotificationDate().equals(lastNotificationScheduled.getNotificationDate()))
        {
            System.out.println("Notification already scheduled: " + notification.toFriendlyString());
            return;
        }

        System.out.println("Scheduling " +  notification.toFriendlyString());

        Date now = new Date();
        long waitSecs = notification.getNotificationDate().after(now) ?
            (notification.getNotificationDate().getTime() - now.getTime())/1000 : // normal case - in future
            1;  // was in the past - do immediately - probably were multiple at same time

        NotificationHandler handler = new NotificationHandler(notification, this);
        scheduler.schedule(handler, waitSecs, SECONDS);

        lastNotificationScheduled = notification;
        System.out.println("Notification scheduled");
    }
}
