package xyz.cleangone.e2.web.vaadin.desktop.org.event.components;

import com.vaadin.ui.VerticalLayout;
import xyz.cleangone.data.aws.dynamo.entity.organization.EventParticipant;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.person.Person;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.cache.EntityLastTouched;
import xyz.cleangone.data.cache.EntityType;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.banner.ActionBar;

import java.util.*;
import java.util.stream.Collectors;


public class RightColLayout extends VerticalLayout
{
    private EntityLastTouched entityLastTouched = EntityLastTouched.getEntityLastTouched();
    private final ActionBar actionBar;

    private SessionManager sessionMgr;
    private OrgManager orgMgr;
    private EventManager eventMgr;
    private TagManager tagMgr;
    private OrgEvent event;
    private User user;

    private OrgEvent prevEvent;
    private User prevUser;
    private Date entitiesSetDate;


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

    public void set()
    {
        if (event == prevEvent &&
            user == prevUser &&
            !entityLastTouched.entityChangedAfter(entitiesSetDate, orgMgr.getOrgId(), EntityType.PersonTag, EntityType.Person) &&
            !entityLastTouched.entityChangedAfter(entitiesSetDate, event, EntityType.Entity, EntityType.PersonTag, EntityType.Participant))
        {
            // nothing has changed - do not reset components;
            return;
        }

        prevEvent = event;
        prevUser = user;
        entitiesSetDate = new Date();

        removeAllComponents();
        setWidth("28em");

        if (!event.getUserCanRegister() && !event.getAcceptDonations() && !event.getAcceptPledges())
        {
            return;
        }

        List<OrgTag> orgTagsVisibleToEvent = tagMgr.getTags(event.getTagIds());
        List<OrgTag> eventTags = tagMgr.getEventTags(OrgTag.TagType.PersonTag, event.getId());

        List<OrgTag> allEventTags = new ArrayList<>(orgTagsVisibleToEvent);
        allEventTags.addAll(eventTags);
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
    }
}