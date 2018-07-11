package xyz.cleangone.e2.web.manager;

import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.lastTouched.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.cache.EntityLastTouchedCache;

import java.util.Date;
import java.util.List;

public class EntityChangeManager
{
    private EntityLastTouchedCache lastTouched = EntityLastTouchedCache.getEntityLastTouchedCache();
    private User user;
    private Organization org;
    private OrgEvent event;
    private int count = -1;
    private Date entitiesSetDate;
    private boolean dateSet;

    public boolean changed(OrgEvent newEvent) { return !unchanged(newEvent); }
    public boolean changed(BaseEntity entity, EntityType... types)
    {
        return !unchanged(entity, types);
    }

    public boolean unchanged(User newUser) { return dateSet && user == newUser; }
    public boolean unchanged(Organization newOrg) { return dateSet && org == newOrg; }
    public boolean unchanged(OrgEvent newEvent) { return dateSet && event == newEvent; }
    public boolean unchanged(int newCount) { return dateSet && count == newCount; }

    public boolean unchanged(String entityId, EntityType... types)
    {
        return dateSet && !lastTouched.entityChangedAfter(entitiesSetDate, entityId, types);
    }

    public boolean unchanged(List<? extends BaseEntity> entities, EntityType type)
    {
        return dateSet && !lastTouched.entitiesChangedAfter(entitiesSetDate, entities, type);
    }

    public boolean unchanged(BaseEntity entity, EntityType... types)
    {
        return entity == null || unchanged(entity.getId(), types);
    }
    public boolean unchangedEntity(String entityId)
    {
        return unchanged(entityId, EntityType.Entity);
    }
    public boolean unchangedEntity(BaseEntity entity)
    {
        return unchanged(entity, EntityType.Entity);
    }
    public boolean unchangedEntity(List<? extends BaseEntity> entities)
    {
        return unchanged(entities, EntityType.Entity);
    }

    public void reset()
    {
        reset(null, null, null, -1);
    }
    public void reset(Organization org)
    {
        reset(null, org, null, -1);
    }
    public void reset(Organization org, int count)
    {
        reset(null, org, null, count);
    }
    public void reset(OrgEvent event)
    {
        reset(null, null, event, -1);
    }
    public void reset(User user)
    {
        reset(user, null, null, -1);
    }
    public void reset(User user, OrgEvent event)
    {
        reset(user, null, event, -1);
    }

    public void reset(User user, Organization org, OrgEvent event, int count)
    {
        this.user = user;
        this.org = org;
        this.event = event;
        this.count = count;
        entitiesSetDate = new Date();
        dateSet = true;
    }

}