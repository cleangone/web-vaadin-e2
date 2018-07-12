package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.actions;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.DateRenderer;
import xyz.cleangone.data.aws.dynamo.entity.action.Action;
import xyz.cleangone.data.aws.dynamo.entity.action.ActionType;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.manager.ActionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.EventsAdminLayout;
import xyz.cleangone.web.vaadin.ui.MessageDisplayer;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static xyz.cleangone.data.aws.dynamo.entity.action.Action.AMOUNT_FIELD;
import static xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity.CREATED_DATE_FIELD;

public class PurchasesAdmin extends ActionsAdmin
{
    public PurchasesAdmin(EventsAdminLayout eventsAdmin, MessageDisplayer msgDisplayer)
    {
        super(eventsAdmin, msgDisplayer);
    }

    public void set()
    {
        OrgEvent event = requireNonNull(eventMgr.getEvent());
        if (unchanged(event)) { return; }

        removeAllComponents();
        List<Action> purchases = actionMgr.getActionsByTargetEvent(event, ActionType.Purchased);

        Component grid = getActionGrid(purchases);
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

        grid.addColumn(Action::getDescription).setCaption("Description");
        grid.addColumn(Action::getTargetPersonFirstLast).setCaption("For Person");

        grid.sort(CREATED_DATE_FIELD.getName(), SortDirection.DESCENDING);

        HeaderRow headerRow = grid.appendHeaderRow();

        grid.setDataProvider(new ListDataProvider<>(actions));

        BigDecimal amountTotal = ActionManager.sumAmount(actions);
        headerRow.getCell(AMOUNT_FIELD.getName()).setHtml("<b>" + Action.getDisplayAmount(amountTotal) + "</b>");

        return grid;
    }
}