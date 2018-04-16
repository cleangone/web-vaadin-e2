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
    private ScheduledFuture<?> future;

    public NotificationScheduler(NotificationManager notificationMgr)
    {
        this.notificationMgr = notificationMgr;
    }

    public void schedule()
    {
        if (future != null) { future.cancel(true); }

        QueuedNotification notification = notificationMgr.getEarliestNotification();
        if (notification == null) { return; }

        System.out.println("Scheduling notification for " + SDF.format(notification.getNotificationDate()));

        Date now = new Date();
        long waitSecs = notification.getNotificationDate().after(now) ?
            (notification.getNotificationDate().getTime() - now.getTime())/1000 : // normal case - in future
            1;  // was in the past - do immediately - probably were multiple at same time

        NotificationHandler handler = new NotificationHandler(notification, this);
        future = scheduler.schedule(handler, waitSecs, SECONDS);
        System.out.println("Notification scheduled");
    }

    public void reset()
    {
        // schedule next one if it exists
        schedule();
    }

    public NotificationManager getNotificationMgr()
    {
        return notificationMgr;
    }
}
