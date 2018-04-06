package xyz.cleangone.e2.web.vaadin.desktop.org.profile;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.DateRenderer;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.action.ActionType;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.ActionManager;
import xyz.cleangone.e2.web.manager.EntityChangeManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.org.BaseAdmin;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.List;

import static xyz.cleangone.data.aws.dynamo.entity.person.User.*;

public class ActionsAdmin extends BaseAdmin
{
    private static ActionType[] DONATION_ACTIONS = { ActionType.Donated, ActionType.Pledged, ActionType.FulfilledPledge };
    private static ActionType[] PURCHASE_ACTIONS = { ActionType.Purchased };

    private final List<ActionType> actionTypes;
    private ActionManager actionMgr;
    private User user;
    private EntityChangeManager changeManager = new EntityChangeManager();


    public ActionsAdmin(MessageDisplayer msgDisplayer, ProfilePageType pageType)
    {
        super(msgDisplayer);
        actionTypes = pageType == ProfilePageType.DONATIONS ? Arrays.asList(DONATION_ACTIONS) : Arrays.asList(PURCHASE_ACTIONS);

        setMargin(true);
        setSpacing(false);
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
            changeManager.unchanged(user.getPersonId(), EntityType.Action))
        {
            return;
        }

        changeManager.reset(user);
        removeAllComponents();

        Component grid = getActionGrid();
        addComponent(grid);
        setExpandRatio(grid, 1.0f);
    }

    private Grid<Action> getActionGrid()
    {
        Grid<Action> grid = new Grid<>();
        grid.setSizeFull();

        grid.addColumn(Action::getCreatedDate).setCaption("Date")
            .setId(CREATED_DATE_FIELD.getName())
            .setRenderer(new DateRenderer(DateFormat.getDateInstance(DateFormat.MEDIUM)));
        grid.addColumn(Action::getTargetEventName).setCaption("Event");
        grid.addColumn(Action::getActionType).setCaption("Action");
        grid.addColumn(Action::getDisplayAmount).setCaption("Amount");
        grid.addColumn(Action::getDescription).setCaption("Description");
        grid.addColumn(Action::getTargetPersonFirstLast).setCaption("For Person");

        grid.sort(CREATED_DATE_FIELD.getName(), SortDirection.DESCENDING);

        List<Action> actions = actionMgr.getActionsBySourcePerson(user.getPersonId(), actionTypes);
        grid.setDataProvider(new ListDataProvider<>(actions));

        return grid;
    }
}