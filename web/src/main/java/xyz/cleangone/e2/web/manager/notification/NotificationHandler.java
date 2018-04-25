package xyz.cleangone.e2.web.manager.notification;

import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.item.SaleStatus;
import xyz.cleangone.data.aws.dynamo.entity.notification.NotificationType;
import xyz.cleangone.data.aws.dynamo.entity.notification.QueuedNotification;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.NotificationManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.event.ItemManager;

import java.text.SimpleDateFormat;
import java.util.Date;


public class NotificationHandler implements Runnable
{
    private static SimpleDateFormat SDF = new SimpleDateFormat("EEE MMM d, hh:mm:ss aaa");

    private final String notificationId;
    private final String orgId;
    private final NotificationScheduler scheduler;

    public NotificationHandler(QueuedNotification notification, NotificationScheduler scheduler)
    {
        notificationId = notification.getId();
        orgId = notification.getOrgId();
        this.scheduler = scheduler;
    }

    public void run()
    {
        processNotification();
        scheduler.schedule();
    }

    private void processNotification()
    {
        NotificationManager notificationMgr = new NotificationManager(orgId);
        QueuedNotification notification = notificationMgr.getNotification(notificationId);

        String date = SDF.format(new Date()) + " -  ";
        if (notification == null)
        {
            System.out.println(date + "Notification{id=" + notificationId + "} already processed");
            return;
        }

        Date adjustedNotificationDate = new Date(notification.getNotificationDate().getTime() - 1000); // adjust 1 sec to handle race condition
        if (adjustedNotificationDate.after(new Date()))
        {
            System.out.println(date + "Bypassing rescheduled " + notification.toFriendlyString());
            return;
        }

        System.out.println(date + "Processing " + notification.toFriendlyString());
        if (notification.getNotificationType() == NotificationType.ItemAuctionClose)
        {
            ItemManager itemMgr = new ItemManager();

            CatalogItem item = itemMgr.getItemById(notification.getItemId());
            if (item.getHighBidId() == null)
            {
                // do something?
                int i=1;
            }
            else
            {
                item.setSaleStatus(SaleStatus.Sold);

                // generate an action
                // send email to winning bidder

                itemMgr.save(item);
            }
        }

        System.out.println(date + "Deleting " + notification.toFriendlyString());
        notificationMgr.delete(notification);
    }
}
