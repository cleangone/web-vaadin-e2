package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event;

import com.vaadin.ui.CheckBoxGroup;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.org.disclosure.BaseOrgDisclosure;
import xyz.cleangone.e2.web.vaadin.util.DescriptionGenerator;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class BaseEventTagsAdmin extends BaseEventAdmin
{
    protected final OrgTag.TagType tagType;

    protected EventManager eventMgr;
    protected OrgManager orgMgr;
    protected TagManager tagMgr;
    protected OrgEvent event;

    protected TagsDisclosure tagsDisclosure;

    public BaseEventTagsAdmin(EventsAdminLayout eventsAdminLayout, OrgTag.TagType tagType, MessageDisplayer msgDisplayer)
    {
        super(eventsAdminLayout, msgDisplayer);

        this.tagType = tagType;
    }

    public void set(SessionManager sessionMgr)
    {
        eventMgr = sessionMgr.getEventManager();
        orgMgr = sessionMgr.getOrgManager();
        tagMgr = orgMgr.getTagManager();
    }

    public void set()
    {
        event = requireNonNull(eventMgr.getEvent());
    }

    protected Component getTagDisclosure(DescriptionGenerator descriptionGenerator)
    {
        tagsDisclosure = new TagsDisclosure(descriptionGenerator);
        return tagsDisclosure;
    }

    protected void setOrgTagsDisclosureCaption() { tagsDisclosure.setDisclosureCaption(); }

    protected class TagsDisclosure extends BaseOrgDisclosure
    {
        DescriptionGenerator descGenerator;
        TagsDisclosure(DescriptionGenerator descGenerator)
        {
            super(new EventTagsAdmin(descGenerator), event);
            this.descGenerator = descGenerator;
            setDisclosureCaption();
        }

        public void setDisclosureCaption()
        {
            List<String> tagIds = eventMgr.getEventTagIds(tagType);
            setDisclosureCaption(descGenerator.numText(tagIds) + " " + descGenerator.text(tagIds) + " visible to the Event");
        }
    }

    protected class EventTagsAdmin extends HorizontalLayout
    {
        final DescriptionGenerator descGenerator;
        List<CheckBoxGroup<OrgTag>> checkBoxGroups = new ArrayList<>();

        EventTagsAdmin(DescriptionGenerator descGenerator)
        {
            this.descGenerator = descGenerator;
            setMargin(false);
            setSpacing(true);

            // split tags into columns
            List<OrgTag> orgTags = tagMgr.getTags(tagType);
            if (orgTags.isEmpty()) { return; }

            List<List<OrgTag>> tagCols = new ArrayList<>();
            List<OrgTag> tagCol = null;
            int colMaxSize = orgTags.size()/3 + 1;
            for (OrgTag tag : orgTags)
            {
                if (tagCol == null)
                {
                    tagCol = new ArrayList<>();
                    tagCols.add(tagCol);
                }

                tagCol.add(tag);
                if (tagCol.size() == colMaxSize) { tagCol = null; }
            }

            List<String> initialSelectedTagIds = eventMgr.getEventTagIds(tagType);
            for (List<OrgTag> tags : tagCols)
            {
                CheckBoxGroup<OrgTag> checkBoxGroup = getCheckBoxGroup(tags, initialSelectedTagIds);
                checkBoxGroups.add(checkBoxGroup);
                addComponent(checkBoxGroup);
            }
        }

        CheckBoxGroup<OrgTag> getCheckBoxGroup(List<OrgTag> orgTags, List<String> initialSelectedTagIds)
        {
            CheckBoxGroup<OrgTag> checkBoxGroup = new CheckBoxGroup<>();
            checkBoxGroup.setItems(orgTags);
            checkBoxGroup.setItemCaptionGenerator(OrgTag::getName);
            checkBoxGroup.addBlurListener(event -> saveTags());

            if (initialSelectedTagIds != null)
            {
                orgTags.stream()
                    .filter(tag -> initialSelectedTagIds.contains(tag.getId()))
                    .forEach(checkBoxGroup::select);
            }

            return checkBoxGroup;
        }

        void saveTags()
        {
            Set<OrgTag> selectedTags = new HashSet<>();
            checkBoxGroups.forEach(group -> selectedTags.addAll(group.getSelectedItems()));

            List<String> tagIds = selectedTags.stream()
                .map(OrgTag::getId)
                .collect(Collectors.toList());

            eventMgr.setEventTagIds(tagIds, tagType);
            eventMgr.save();

            msgDisplayer.displayMessage(descGenerator.plural() + " saved");
            setOrgTagsDisclosureCaption();
        }
    }

}