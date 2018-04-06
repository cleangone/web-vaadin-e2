package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.vaadin.navigator.View;
import com.vaadin.ui.Label;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.*;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static xyz.cleangone.e2.web.vaadin.util.PageUtils.*;
import static xyz.cleangone.e2.web.vaadin.util.PageUtils.getPageDisplayType;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class EventPage extends BaseEventPage implements View
{
    private static final Logger LOG = Logger.getLogger(EventPage.class.getName());
    public static final String NAME = "Event";
    private static String STYLE_WORD_WRAP = "wordWrap";

    protected PageDisplayType set(SessionManager sessionMgr)
    {
        super.set(sessionMgr);

        setMenuLeftStyle();
        return set();
    }

    protected String getPageName()
    {
        return event == null ? "Event" : event.getName();
    }

    protected PageDisplayType set()
    {
        PageDisplayType leftDisplayType = setLeftLayout();
        PageDisplayType centerDisplayType = setCenterLayout();
        PageDisplayType rightDisplayType = rightLayout.set();

        return getPageDisplayType(leftDisplayType, centerDisplayType, rightDisplayType);
    }

    private PageDisplayType setCenterLayout()
    {
        boolean showFulfillPledgesPanel = FulfillPledgesPanel.panelHasContent(event, user);
        if (!showFulfillPledgesPanel &&
            centerLayout.getComponentCount() == 1 &&
            !updateDateChanged(event))
        {
            // layout just showing html, which has not changed
            return PageDisplayType.NoChange;
        }

        setUpdateDate(event);
        centerLayout.removeAllComponents();

        FulfillPledgesPanel pledgesPanel = new FulfillPledgesPanel(sessionMgr, actionBar);
        if (pledgesPanel.unfulfilledPledgesExist()) { centerLayout.addComponents(new Label(), pledgesPanel); }

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

        return PageDisplayType.ObjectRetrieval;
    }
}