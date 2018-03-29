package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.vaadin.navigator.View;
import com.vaadin.ui.Label;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.*;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class EventPage extends BaseEventPage implements View
{
    private static final Logger LOG = Logger.getLogger(EventPage.class.getName());
    public static final String NAME = "Event";
    private static String STYLE_WORD_WRAP = "wordWrap";


    protected void set(SessionManager sessionMgr)
    {
        super.set(sessionMgr);

        setMenuLeftStyle();
        set();
    }

    protected void set()
    {
        setLeftLayout();
        setCenterLayout();
        rightLayout.set();
    }

    private void setCenterLayout()
    {
        boolean showFulfillPledgesPanel = FulfillPledgesPanel.panelHasContent(event, user);
        if (!showFulfillPledgesPanel &&
            centerLayout.getComponentCount() == 1 &&
            !updateDateChanged(event))
        {
            // layout just showing html, which has not changed
            return;
        }

        setUpdateDate(event);
        centerLayout.removeAllComponents();

        FulfillPledgesPanel panel = new FulfillPledgesPanel(sessionMgr, actionBar);
        if (panel.unfulfilledPledgesExist()) { centerLayout.addComponents(new Label(), panel); }

        Label label = getHtmlLabel(event.getIntroHtml());
        label.setStyleName(STYLE_WORD_WRAP);

        centerLayout.addComponent(label);

        // for now, items without categories will be displayed on event page
        List<CatalogItem> items = itemMgr.getItems().stream()
            .filter(item -> item.getCategoryIds().isEmpty())
            .collect(Collectors.toList());

        if (!items.isEmpty())
        {
            centerLayout.addComponent(new CatalogLayout(items, eventMgr, itemMgr.getImageManager()));
        }
    }
}