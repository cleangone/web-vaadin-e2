package xyz.cleangone.e2.web.vaadin.desktop.org.event.components;

import com.vaadin.ui.VerticalLayout;
import xyz.cleangone.data.aws.dynamo.entity.lastTouched.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.organization.TagType;
import xyz.cleangone.data.aws.dynamo.entity.person.Person;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.e2.web.manager.EntityChangeManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.ActionBar;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;

import java.util.*;
import java.util.stream.Collectors;

public class RightColLayout extends VerticalLayout
{
    private final ActionBar actionBar;

    private SessionManager sessionMgr;
    private OrgManager orgMgr;
    private EventManager eventMgr;
    private TagManager tagMgr;
    private OrgEvent event;
    private User user;
    private int colWidth;
    private EntityChangeManager changeManager = new EntityChangeManager();

    public RightColLayout(ActionBar actionBar)
    {
        this.actionBar = actionBar;
        setWidthUndefined();
    }

    public void set(SessionManager sessionMgr)
    {
        this.sessionMgr = sessionMgr;
        orgMgr = sessionMgr.getOrgManager();
        eventMgr = sessionMgr.getEventManager();
        tagMgr = orgMgr.getTagManager();

        event = eventMgr.getEvent();
        user = sessionMgr.getUser();
    }

    public PageDisplayType set()
    {
        if (changeManager.unchanged(user) &&
            changeManager.unchanged(event) &&
            changeManager.unchanged(orgMgr.getOrgId(), EntityType.PersonTag, EntityType.Person) &&
            changeManager.unchanged(event, EntityType.Entity, EntityType.PersonTag, EntityType.Participant))
        {
            return PageDisplayType.NoChange;
        }

        changeManager.reset(user, event);
        removeAllComponents();
        setColWidth(400);

        if (!event.getUserCanRegister() && !event.getAcceptDonations() && !event.getAcceptPledges())
        {
            return PageDisplayType.NoRetrieval;
        }

//        List<OrgTag> orgTagsVisibleToEvent = tagMgr.getTags(event.getTagIds());
        List<OrgTag> eventTags = tagMgr.getEventVisibleTags(TagType.PERSON_TAG_TAG_TYPE, event);
        List<OrgTag> allEventTags = new ArrayList<>(eventTags);
//        allEventTags.addAll(eventTags);
        Collections.sort(allEventTags);

        if (event.getUserCanRegister()) { addComponent(new RegisterPanel(allEventTags, sessionMgr, actionBar)); }

        if (event.getAcceptDonations() || event.getAcceptPledges())
        {
            Set<String> userVisibleEventTagIds = allEventTags.stream()
                .filter(OrgTag::getUserVisible)
                .map(OrgTag::getId)
                .collect(Collectors.toSet());

            Map<String, Person> peopleById = orgMgr.getPeopleByIdMap();
            List<EventParticipant> participants = eventMgr.getEventParticipants();
            participants.forEach(p -> p.setPerson(peopleById.get(p.getPersonId())));

            List<EventParticipant> visibleParticipants = new ArrayList<>(participants.stream()
                .filter(p -> p.getPerson() != null)
                .filter(p -> p.getPerson().includesOneOfTags(userVisibleEventTagIds))
                .collect(Collectors.toList()));

            visibleParticipants.sort((p1, p2) -> p1.getLastCommaFirst().compareToIgnoreCase(p2.getLastCommaFirst()));

            if (event.getAcceptDonations()) { addComponent(new DonationPanel(visibleParticipants, sessionMgr, actionBar)); }
            if (event.getAcceptPledges()) { addComponent(new PledgePanel(visibleParticipants, sessionMgr, actionBar)); }
        }

        return PageDisplayType.ObjectRetrieval;
    }

    public int getColWidth()
    {
        return colWidth;
    }
    public void setColWidth(int colWidth)
    {
        this.colWidth = colWidth;
        setWidth(colWidth + "px");
    }
}