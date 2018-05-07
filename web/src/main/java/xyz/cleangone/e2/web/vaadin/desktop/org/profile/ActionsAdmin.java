package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.DateRenderer;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.action.ActionType;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.ActionManager;
import xyz.cleangone.e2.web.manager.EntityChangeManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static xyz.cleangone.data.aws.dynamo.entity.person.User.*;

public class ActionsAdmin extends BaseAdmin
{
    private static ActionType[] DONATION_ACTIONS = { ActionType.Donated, ActionType.Pledged, ActionType.FulfilledPledge };
    private static ActionType[] PURCHASE_ACTIONS = { ActionType.Purchased };
    private static ActionType[] BID_ACTIONS = { ActionType.Bid };

    private final ProfilePageType pageType;
    private ActionManager actionMgr;
    private User user;
    private EntityChangeManager changeManager = new EntityChangeManager();

    private List<Action> actions = new ArrayList<>();
    private Grid<Action> actionGrid;

    public ActionsAdmin(MessageDisplayer msgDisplayer, ProfilePageType pageType)
    {
        super(msgDisplayer);
        this.pageType = pageType;

        setMargin(new MarginInfo(true, false, false, false)); // T/R/B/L margins
        setHeight("100%");

        actionGrid = getActionGrid();
        addComponent(actionGrid);
        setExpandRatio(actionGrid, 1.0f);
    }

    public void set(SessionManager sessionMgr)
    {
        actionMgr = sessionMgr.getOrgManager().getActionManager();
        user = sessionMgr.getPopulatedUserManager().getUser();

        set();
    }

    public void set()
    {
        if (changeManager.unchanged(user) &&
            changeManager.unchanged(user.getId(), EntityType.Action))
        {
            return;
        }

        changeManager.reset(user);

        actions.clear();
        actions.addAll(actionMgr.getActionsBySourcePerson(user.getId(), Arrays.asList(getActionTypes())));
        actionGrid.setHeightByRows(actions.size() > 5 ? actions.size()+1 : 5);
    }

    private ActionType[] getActionTypes()
    {
        if (pageType == ProfilePageType.DONATIONS) { return DONATION_ACTIONS; }
        else if (pageType == ProfilePageType.PURCHASES) { return PURCHASE_ACTIONS; }
        else if (pageType == ProfilePageType.BIDS) { return BID_ACTIONS; }
        else return new ActionType[] {};
    }

    private Grid<Action> getActionGrid()
    {
        Grid<Action> grid = new Grid<>();
        grid.setWidth("100%");

        // todo - need date/time
        grid.addColumn(Action::getCreatedDate).setCaption("Date")
            .setId(CREATED_DATE_FIELD.getName())
            .setRenderer(new DateRenderer(DateFormat.getDateInstance(DateFormat.MEDIUM)));
        grid.addColumn(Action::getTargetEventName).setCaption("Event");
        grid.addColumn(Action::getActionType).setCaption("Action");
        grid.addColumn(Action::getDisplayAmount).setCaption("Amount");
        grid.addColumn(Action::getDescription).setCaption("Description");
        if (pageType == ProfilePageType.DONATIONS) { grid.addColumn(Action::getTargetPersonFirstLast).setCaption("For Person"); }

        grid.sort(CREATED_DATE_FIELD.getName(), SortDirection.DESCENDING);
        grid.setDataProvider(new ListDataProvider<>(actions));

        return grid;
    }
}