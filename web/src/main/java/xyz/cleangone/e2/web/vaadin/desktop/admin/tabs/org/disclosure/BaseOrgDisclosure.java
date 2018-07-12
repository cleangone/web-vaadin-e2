package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.disclosure;

import com.vaadin.ui.AbstractOrderedLayout;
import xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.web.vaadin.disclosure.BaseDisclosure;


public abstract class BaseOrgDisclosure extends BaseDisclosure
{
    // todo - slightly ulgy
    protected BaseOrg baseOrg;
    protected Organization org;
    protected OrgEvent orgEvent;

    public BaseOrgDisclosure(AbstractOrderedLayout layout, BaseOrg baseOrg)
    {
        this(null, layout, baseOrg);
    }

    public BaseOrgDisclosure(String caption, AbstractOrderedLayout layout, OrgEvent orgEvent)
    {
        super(caption, layout);
        this.baseOrg = orgEvent;
        this.orgEvent = orgEvent;
    }

    public BaseOrgDisclosure(String caption, AbstractOrderedLayout layout, Organization org)
    {
        super(caption, layout);
        this.baseOrg = org;
        this.org = org;
    }

    public BaseOrgDisclosure(String caption, AbstractOrderedLayout layout, BaseOrg baseOrg)
    {
        super(caption, layout);
        this.baseOrg = baseOrg;
    }

}