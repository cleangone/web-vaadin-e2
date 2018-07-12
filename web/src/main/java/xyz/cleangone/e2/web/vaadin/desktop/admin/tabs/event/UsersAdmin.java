package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event;

import com.vaadin.event.selection.MultiSelectionEvent;
import com.vaadin.event.selection.MultiSelectionListener;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.HeaderRow;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;

import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.web.vaadin.ui.EntityGrid;
import xyz.cleangone.web.vaadin.util.CountingDataProvider;
import xyz.cleangone.web.vaadin.ui.MessageDisplayer;
import xyz.cleangone.web.vaadin.util.MultiFieldFilter;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant.*;
import static xyz.cleangone.data.aws.dynamo.entity.person.User.*;
import static xyz.cleangone.web.vaadin.util.VaadinUtils.*;

public class UsersAdmin extends BaseEventAdmin implements MultiSelectionListener<User>
{
    private EventManager eventMgr;
    private OrgManager orgMgr;
    private TagManager tagMgr;
    private UserManager userMgr;
    protected OrgEvent event;

    private List<User> selectedUsers;

    public UsersAdmin(EventsAdminLayout eventsAdminLayout, MessageDisplayer msgDisplayer)
    {
        super(eventsAdminLayout, msgDisplayer);
        setLayout(this, MARGIN_TR, SPACING_TRUE, SIZE_FULL, WIDTH_100_PCT);
    }

    public void set(SessionManager sessionMgr)
    {
        eventMgr = sessionMgr.getEventManager();
        orgMgr = sessionMgr.getOrgManager();
        tagMgr = orgMgr.getTagManager();
        userMgr = sessionMgr.getUserManager();
    }

    public void set()
    {
        event = requireNonNull(eventMgr.getEvent());

        removeAllComponents();

        Component grid = new UsersGrid(this);
        addComponents(getUpdateTagsLayout(), grid, new Label());
        setExpandRatio(grid, 1.0f);
    }

    private Component getUpdateTagsLayout()
    {
        HorizontalLayout leftLayout = new HorizontalLayout();
        HorizontalLayout rightLayout = new HorizontalLayout();

        HorizontalLayout barLayout = new HorizontalLayout();
        barLayout.setSizeUndefined();
        barLayout.setWidth("100%");
        barLayout.addComponents(leftLayout, rightLayout);
        barLayout.setExpandRatio(leftLayout, 1.0f);
        barLayout.setComponentAlignment(rightLayout, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));

        return barLayout;
    }

    @Override
    public void selectionChange(MultiSelectionEvent<User> event)
    {

    }

    private class UsersGrid extends EntityGrid<User>
    {
        UsersGrid(MultiSelectionListener<User> listener)
        {
            // get enabled users that are not already an admin
            List<User> users = orgMgr.getUsers().stream()
                .filter(u -> u.getEnabled())
                .filter(u -> !u.isOrgAdmin(orgMgr.getOrgId())) // todo - why do this?
                .collect(Collectors.toList());

            setSizeFull();

            addColumn(LAST_COMMA_FIRST_FIELD, User::getLastCommaFirst, 1);
            addColumn(this::isAdmin)
                .setId(ADMIN_FIELD.getName()).setCaption(ADMIN_FIELD.getDisplayName())
                .setEditorComponent(new CheckBox(), this::setAdmin);

            setColumnReorderingAllowed(true);
            setSelectionMode(Grid.SelectionMode.MULTI);
            asMultiSelect().addSelectionListener(listener);

            getEditor().setEnabled(true);
            getEditor().addSaveListener(event -> {
                User user = event.getBean();
                userMgr.save(user);
                msgDisplayer.displayMessage("User updates saved");
                set();
            });

            CountingDataProvider<User> dataProvider = new CountingDataProvider<>(users, countLabel);
            setDataProvider(dataProvider);

            HeaderRow filterHeader = appendHeaderRow();
            setColumnFiltering(filterHeader, dataProvider);
        }

        private boolean isAdmin(User user)
        {
            return user.isEventAdmin(event.getOrgId(), event.getId());
        }
        private void setAdmin(User user, boolean isAdmin)
        {
            if (isAdmin) { user.addAdminPrivledge(event); }
            else { user.removeAdminPrivledge(event); }
        }

        private void setColumnFiltering(HeaderRow filterHeader, CountingDataProvider<User> dataProvider)
        {
            MultiFieldFilter<User> filter = new MultiFieldFilter<>(dataProvider);
            addFilterField(LAST_COMMA_FIRST_FIELD, User::getLastCommaFirst, filter, filterHeader);
        }
    }
}