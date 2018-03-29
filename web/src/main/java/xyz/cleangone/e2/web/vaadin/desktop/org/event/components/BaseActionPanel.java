package xyz.cleangone.e2.web.vaadin.desktop.org.event.components;

import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.ActionManager;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.banner.ActionBar;

public class BaseActionPanel extends Panel
{
    protected final SessionManager sessionMgr;
    protected final ActionBar actionBar;
    protected final AbstractOrderedLayout panelLayout = new VerticalLayout();

    protected final EventManager eventMgr;
    protected final ActionManager actionMgr;
    protected final OrgEvent event;
    protected final User user;


    public BaseActionPanel(SessionManager sessionMgr, ActionBar actionBar)
    {
        super();
        this.sessionMgr = sessionMgr;
        this.actionBar = actionBar;

        eventMgr = sessionMgr.getEventManager();
        actionMgr = sessionMgr.getOrgManager().getActionManager();
        event = eventMgr.getEvent();

        UserManager userMgr = sessionMgr.getUserManager();
        user = userMgr.getUser();

        panelLayout.setDefaultComponentAlignment(new Alignment(AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER));

        setWidth("100%");
        setContent(panelLayout);
    }

    public BaseActionPanel(String title, SessionManager sessionMgr, ActionBar actionBar)
    {
        this(sessionMgr, actionBar);
        setCaption(title);
    }

    protected FormLayout getFormLayout()
    {
        FormLayout layout = new FormLayout();
        layout.setMargin(false);
        layout.setSizeUndefined();

        return layout;
    }

    protected void navigateTo(String pageName) { getUI().getNavigator().navigateTo(pageName); }

}
