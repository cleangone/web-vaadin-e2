package xyz.cleangone.e2.web.vaadin.desktop.broadcast;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.UI;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.ItemPage;

public abstract class BroadcastListeningUI extends UI implements BroadcastListener
{
    private MyViewChangeListener viewChangeListener;

    protected void initBroadcastListener()
    {
        Broadcaster.register(this);

        viewChangeListener = new MyViewChangeListener();
        getNavigator().addViewChangeListener(viewChangeListener);
    }

    @Override
    public void receiveBroadcast(final BroadcastNotification notification)
    {
        // lock the session to execute logic safely
        access(new Runnable() {
            @Override
            public void run()
            {
                // todo - bypass if broadcast org not the same as one in session
                String orgId = notification.getOrgId();

                View currentView = viewChangeListener.getCurrentView();
                if (currentView instanceof ItemPage)
                {
                    ItemPage itemPage = (ItemPage)currentView;
                    if (notification.hasItemId(itemPage.getItemId())) { itemPage.reset(); }
                }
            }
        });
    }

    @Override
    public void detach()
    {
        Broadcaster.unregister(this);
        super.detach();
    }

    class MyViewChangeListener implements ViewChangeListener
    {
        View currentView;

        public void afterViewChange(ViewChangeEvent event)
        {
            currentView = event.getNewView();
        }
        public boolean beforeViewChange(ViewChangeEvent event)
        {
            return true;
        }

        View getCurrentView()
        {
            return currentView;
        }
    }
}
