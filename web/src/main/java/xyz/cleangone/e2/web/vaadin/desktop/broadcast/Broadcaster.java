package xyz.cleangone.e2.web.vaadin.desktop.broadcast;

import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Broadcaster implements Serializable
{
    private static ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static LinkedList<BroadcastListener> LISTENERS = new LinkedList<>();

    public static synchronized void register(BroadcastListener listener)
    {
        LISTENERS.add(listener);
    }
    public static synchronized void unregister(BroadcastListener listener)
    {
        LISTENERS.remove(listener);
    }

    public static void broadcast(CatalogItem item)
    {
        broadcast(item.getOrgId(), item.getId());
    }
    public static void broadcast(String orgId, String itemId)
    {
        broadcast(new BroadcastNotification(orgId, itemId));
    }
    public static synchronized void broadcast(final BroadcastNotification notification)
    {
        for (final BroadcastListener listener: LISTENERS)
        {
            EXECUTOR.execute(new Runnable() {
                @Override
                public void run() { listener.receiveBroadcast(notification); }
            });
        }
    }
}