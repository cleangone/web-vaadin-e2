package xyz.cleangone.e2.web.manager.notification;

import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.item.SaleStatus;
import xyz.cleangone.data.aws.dynamo.entity.notification.NotificationType;
import xyz.cleangone.data.aws.dynamo.entity.notification.QueuedNotification;
import xyz.cleangone.data.manager.NotificationManager;
import xyz.cleangone.data.manager.event.ItemManager;
import xyz.cleangone.e2.web.vaadin.desktop.broadcast.Broadcaster;

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

        if (notification == null)
        {
            log("Notification{id=" + notificationId + "} already processed");
            return;
        }

        Date adjustedNotificationDate = new Date(notification.getNotificationDate().getTime() - 1000); // adjust 1 sec to handle race condition
        if (adjustedNotificationDate.after(new Date()))
        {
            log("Bypassing rescheduled " + notification.toFriendlyString());
            return;
        }

        log("Processing " + notification.toFriendlyString());
        if (notification.getNotificationType() == NotificationType.ItemAuctionClose)
        {
            processItemNotification(notification);
        }

        log("Deleting " + notification.toFriendlyString());
        notificationMgr.delete(notification);
    }

    private void processItemNotification(QueuedNotification notification)
    {
        if (notification.getItemId() == null)
        {
            log("Bypassing notification - itemId not set");
            return;
        }

        ItemManager itemMgr = new ItemManager();
        CatalogItem item = itemMgr.getItemById(notification.getItemId());
        if (item == null)
        {
            log("Bypassing notification - item{id=" + notification.getItemId() + "} not found");
            return;
        }

        if (!orgId.equals(item.getOrgId()))
        {
            log("Bypassing notification - notification.orgId does not match item{id=" + item.getId() + ", orgId=" + item.getOrgId() + "}");
            return;
        }

        if (item.hasBids())
        {
            item.setSaleStatus(SaleStatus.Sold);
            itemMgr.save(item);

            // generate an action
            // send email to winning bidder
        }
        else if (item.isAuction()) // drop w/o bids is still available
        {
            item.setSaleStatus(SaleStatus.Unsold);
            itemMgr.save(item);
        }

        Broadcaster.broadcast(item);
    }

    // todo - poor man's logger
    private void log(String msg)
    {
        System.out.println(SDF.format(new Date()) + " -  " + msg);
    }
}
