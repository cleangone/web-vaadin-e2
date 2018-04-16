package xyz.cleangone.e2.web.manager.notification;

import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.notification.NotificationType;
import xyz.cleangone.data.aws.dynamo.entity.notification.QueuedNotification;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.NotificationManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.event.ItemManager;

import javax.management.Notification;
import java.text.SimpleDateFormat;
import java.util.Date;


public class NotificationHandler implements Runnable
{
    private static SimpleDateFormat SDF = new SimpleDateFormat("EEE MMM d, hh:mm:ss aaa");

    private final QueuedNotification notification;
    private final NotificationScheduler scheduler;

    public NotificationHandler(QueuedNotification notification, NotificationScheduler scheduler)
    {
        this.notification = notification;
        this.scheduler = scheduler;
    }

    public void run()
    {
        System.out.println(SDF.format(new Date()) + " - Processing " +
            notification.getNotificationType() + " notification for " + SDF.format(notification.getNotificationDate()));

        if (notification.getNotificationType() == NotificationType.ItemAuctionClose)
        {
            ItemManager itemMgr = new ItemManager();
            CatalogItem item = itemMgr.getDao().getById(notification.getItemId());

            // Q - what to do?

            // todo - will there be an item.auctionComplete?


        }

        NotificationManager notificationMgr = scheduler.getNotificationMgr();
        notificationMgr.delete(notification);

        scheduler.reset();
    }

}
