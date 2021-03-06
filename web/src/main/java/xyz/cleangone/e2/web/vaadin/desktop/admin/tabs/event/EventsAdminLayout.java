package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event;

import com.vaadin.ui.*;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.actions.DonationsAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.actions.PurchasesAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.item.ItemsAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.participant.ParticipantsAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.AdminPageType;
import xyz.cleangone.web.vaadin.ui.MessageDisplayer;

import java.util.HashMap;
import java.util.Map;

import static xyz.cleangone.web.vaadin.util.VaadinUtils.*;

public class EventsAdminLayout extends HorizontalLayout
{
    private final NavCol navCol;
    private final Map<AdminPageType, BaseAdmin> adminComponents = new HashMap<>();
    private final VerticalLayout mainLayout = new VerticalLayout();

    public EventsAdminLayout(MessageDisplayer msgDisplayer)
    {
        setLayout(this, MARGIN_FALSE, SIZE_FULL, BACK_BLUE);

        navCol = new NavCol(this);
        adminComponents.put(EventAdminPageType.EVENTS, new EventsAdmin(this, msgDisplayer));
        adminComponents.put(EventAdminPageType.GENERAL, new GeneralAdmin(msgDisplayer));
        adminComponents.put(EventAdminPageType.PARTICIPANTS, new ParticipantsAdmin(this, msgDisplayer));
        adminComponents.put(EventAdminPageType.ITEMS, new ItemsAdmin(this, msgDisplayer));
        adminComponents.put(EventAdminPageType.DATES, new DatesAdmin(this, msgDisplayer));
        adminComponents.put(EventAdminPageType.DONATIONS, new DonationsAdmin(this, msgDisplayer));
        adminComponents.put(EventAdminPageType.PURCHASES, new PurchasesAdmin(this, msgDisplayer));
        adminComponents.put(EventAdminPageType.USERS, new UsersAdmin(this, msgDisplayer));

        setLayout(mainLayout, MARGIN_FALSE, SIZE_FULL, BACK_RED);
        mainLayout.setHeight((UI.getCurrent().getPage().getBrowserWindowHeight() - 100) + "px");  // hack - like navCol

        // note - spacing set dynamically in setAdminPage

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

    public void setAdminPage(AdminPageType pageType)
    {
        navCol.setLinks(pageType);

        BaseAdmin component = adminComponents.get(pageType);
        component.set();

        setSpacing(pageType != EventAdminPageType.ITEMS && pageType != EventAdminPageType.PARTICIPANTS);

        mainLayout.removeAllComponents();
        mainLayout.addComponent(component);
    }
}