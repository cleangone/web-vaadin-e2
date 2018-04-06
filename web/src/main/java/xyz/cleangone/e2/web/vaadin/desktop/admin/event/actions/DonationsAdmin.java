package xyz.cleangone.e2.web.vaadin.desktop.admin.event.actions;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.DateRenderer;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.action.ActionType;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.manager.ActionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.event.EventsAdminLayout;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static xyz.cleangone.data.aws.dynamo.entity.action.Action.AMOUNT_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.action.Action.ESTIMATED_AMOUNT_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity.CREATED_DATE_FIELD;

public class DonationsAdmin extends ActionsAdmin
{
    private OrgEvent event;

    public DonationsAdmin(EventsAdminLayout eventsAdmin, MessageDisplayer msgDisplayer)
    {
        super(eventsAdmin, msgDisplayer);
    }

    public void set()
    {
        event = requireNonNull(eventMgr.getEvent());
        if (unchanged(event)) { return; }

        removeAllComponents();
        List<Action> allActions = actionMgr.getActionsByTargetEvent(event);

        // get pledges
        List<Action> pledges = allActions.stream()
            .filter(a -> a.getActionType() == ActionType.Pledged)
            .collect(Collectors.toList());

        // get pledges that have been fulfilled
        Set<String> fulfilledPledgeIds = allActions.stream()
            .filter(a -> a.getActionType() == ActionType.FulfilledPledge)
            .map(Action::getReferenceActionId)
            .collect(Collectors.toSet());

        // omit pledges that have been fulfilled
        List<Action> actions = allActions.stream()
            .filter(a -> !fulfilledPledgeIds.contains(a.getId()))
            .collect(Collectors.toList());

        Component grid = getActionGrid(actions);
        addComponents(grid, new Label());
        setExpandRatio(grid, 1.0f);
    }

    private Grid<Action> getActionGrid(List<Action> actions)
    {
        Grid<Action> grid = new Grid<>();
        grid.setSizeFull();

        grid.addColumn(Action::getCreatedDate).setCaption("Date")
            .setId(CREATED_DATE_FIELD.getName())
            .setRenderer(new DateRenderer(DateFormat.getDateInstance(DateFormat.MEDIUM)));
        grid.addColumn(Action::getActionType).setCaption("Action");
        grid.addColumn(Action::getSourcePersonLastCommaFirst).setCaption("By Person");
        grid.addColumn(Action::getDisplayAmount).setCaption(AMOUNT_FIELD.getDisplayName()).setId(AMOUNT_FIELD.getName());
        grid.addColumn(this::getEstimatedAmountDisplay).setCaption(ESTIMATED_AMOUNT_FIELD.getDisplayName()).setId(ESTIMATED_AMOUNT_FIELD.getName());

        grid.addColumn(Action::getDescription).setCaption("Description");
        grid.addColumn(Action::getTargetPersonFirstLast).setCaption("For Person");

        grid.sort(CREATED_DATE_FIELD.getName(), SortDirection.DESCENDING);

        HeaderRow headerRow = grid.appendHeaderRow();

        grid.setDataProvider(new ListDataProvider<>(actions));

        BigDecimal amountTotal = ActionManager.sumAmount(actions);
        BigDecimal estimatedAmountTotal = sumEstimatedAmount(actions);
        headerRow.getCell(AMOUNT_FIELD.getName()).setHtml("<b>" + Action.getDisplayAmount(amountTotal) + "</b>");
        headerRow.getCell(ESTIMATED_AMOUNT_FIELD.getName()).setHtml("<b>" + Action.getDisplayAmount(estimatedAmountTotal) + "</b>");

        return grid;
    }

    private String getEstimatedAmountDisplay(Action action)
    {
        return Action.getDisplayAmount(getEstimatedAmount(action));
    }

    private BigDecimal getEstimatedAmount(Action action)
    {
        if (action.getActionType() == ActionType.Donated || action.getActionType() == ActionType.FulfilledPledge)
        {
            return action.getAmount();
        }
        else if (action.getActionType() == ActionType.Pledged)
        {
            return action.getIterationAmount().multiply(new BigDecimal(event.getEstimatedIterations()));
        }

        return null;
    }

    // todo - inefficient - loops through items twice
    public BigDecimal sumEstimatedAmount(List<Action> actions)
    {
        return actions.stream()
            .map(this::getEstimatedAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}