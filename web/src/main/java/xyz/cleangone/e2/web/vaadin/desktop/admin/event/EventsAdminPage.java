package xyz.cleangone.e2.web.vaadin.desktop.admin.event;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.event.actions.DonationsAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.event.actions.PurchasesAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import java.util.HashMap;
import java.util.Map;

public class EventsAdminPage extends HorizontalLayout
{
    private final NavCol navCol;
    private final Map<EventAdminPageType, BaseAdmin> adminComponents = new HashMap<>();
    private final VerticalLayout mainLayout = new VerticalLayout();

    public EventsAdminPage(MessageDisplayer msgDisplayer)
    {
        navCol = new NavCol(this);
        adminComponents.put(EventAdminPageType.EVENTS, new EventsAdmin(this, msgDisplayer));
        adminComponents.put(EventAdminPageType.GENERAL, new GeneralAdmin(msgDisplayer));
        adminComponents.put(EventAdminPageType.PARTICIPANTS, new ParticipantsAdmin(this, msgDisplayer));
        adminComponents.put(EventAdminPageType.TAGS, new TagsAdmin(this, OrgTag.TagType.PersonTag, msgDisplayer));
        adminComponents.put(EventAdminPageType.ITEMS, new ItemsAdmin(this, msgDisplayer));
        adminComponents.put(EventAdminPageType.DATES, new DatesAdmin(this, msgDisplayer));
        adminComponents.put(EventAdminPageType.DONATIONS, new DonationsAdmin(this, msgDisplayer));
        adminComponents.put(EventAdminPageType.PURCHASES, new PurchasesAdmin(this, msgDisplayer));
        adminComponents.put(EventAdminPageType.ROLES, new TagsAdmin(this, OrgTag.TagType.UserRole, msgDisplayer));
        adminComponents.put(EventAdminPageType.USERS, new UsersAdmin(this, msgDisplayer));

        mainLayout.setMargin(new MarginInfo(false, true, false, false)); // T/R/B/L margins

        setSizeFull();
        setMargin(false);
        setSpacing(true);

        addComponents(navCol, mainLayout);
        setExpandRatio(mainLayout, 1.0f);
    }

    public void set(SessionManager sessionMgr)
    {
        navCol.set(sessionMgr);

        UI ui = getUI();
        for (BaseAdmin component : adminComponents.values())
        {
            component.set(sessionMgr, ui);
        }

        // start with display of all events
        setAdminPage(EventAdminPageType.EVENTS);
    }

    public void setAdminPage(EventAdminPageType pageType)
    {
        navCol.setAdminLinks(pageType);

        BaseAdmin component = adminComponents.get(pageType);
        component.set();
        mainLayout.removeAllComponents();
        mainLayout.addComponent(component);
    }
}