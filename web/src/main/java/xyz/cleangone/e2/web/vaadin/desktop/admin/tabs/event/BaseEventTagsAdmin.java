package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event;

import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.web.vaadin.ui.MessageDisplayer;

import static java.util.Objects.requireNonNull;

public class BaseEventTagsAdmin extends BaseEventAdmin
{
    protected static String ADMIN_GRID_STYLE_NAME = "admin";

    protected final String tagTypeName;

    protected EventManager eventMgr;
    protected OrgManager orgMgr;
    protected TagManager tagMgr;
    protected OrgEvent event;

//    protected TagsDisclosure tagsDisclosure;

    public BaseEventTagsAdmin(EventsAdminLayout eventsAdminLayout, String tagTypeName, MessageDisplayer msgDisplayer)
    {
        super(eventsAdminLayout, msgDisplayer);

        this.tagTypeName = tagTypeName;
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

//    protected Component getTagDisclosure(DescriptionGenerator descriptionGenerator)
//    {
//        tagsDisclosure = new TagsDisclosure(descriptionGenerator);
//        return tagsDisclosure;
//    }

//    protected void setOrgTagsDisclosureCaption() { tagsDisclosure.setDisclosureCaption(); }

//    protected class TagsDisclosure extends BaseOrgDisclosure
//    {
//        DescriptionGenerator descGenerator;
//        TagsDisclosure(DescriptionGenerator descGenerator)
//        {
//            super(new EventTagsAdmin(descGenerator), event);
//            this.descGenerator = descGenerator;
//            setDisclosureCaption();
//        }
//
//        public void setDisclosureCaption()
//        {
//            List<String> tagIds = eventMgr.getEventTagIds(tagTypeName);
//            setDisclosureCaption(descGenerator.numText(tagIds) + " " + descGenerator.text(tagIds) + " visible to the Event");
//        }
//    }

//    protected class EventTagsAdmin extends HorizontalLayout
//    {
//        final DescriptionGenerator descGenerator;
//        List<CheckBoxGroup<OrgTag>> checkBoxGroups = new ArrayList<>();
//
//        EventTagsAdmin(DescriptionGenerator descGenerator)
//        {
//            this.descGenerator = descGenerator;
//            setMargin(false);
//            setSpacing(true);
//
//            // split tags into columns
//            List<OrgTag> orgTags = tagMgr.getTags(tagTypeName);
//            if (orgTags.isEmpty()) { return; }
//
//            List<List<OrgTag>> tagCols = new ArrayList<>();
//            List<OrgTag> tagCol = null;
//            int colMaxSize = orgTags.size()/3 + 1;
//            for (OrgTag tag : orgTags)
//            {
//                if (tagCol == null)
//                {
//                    tagCol = new ArrayList<>();
//                    tagCols.add(tagCol);
//                }
//
//                tagCol.add(tag);
//                if (tagCol.size() == colMaxSize) { tagCol = null; }
//            }
//
//            List<String> initialSelectedTagIds = eventMgr.getEventTagIds(tagTypeName);
//            for (List<OrgTag> tags : tagCols)
//            {
//                CheckBoxGroup<OrgTag> checkBoxGroup = getCheckBoxGroup(tags, initialSelectedTagIds);
//                checkBoxGroups.add(checkBoxGroup);
//                addComponent(checkBoxGroup);
//            }
//        }
//
//        CheckBoxGroup<OrgTag> getCheckBoxGroup(List<OrgTag> orgTags, List<String> initialSelectedTagIds)
//        {
//            CheckBoxGroup<OrgTag> checkBoxGroup = new CheckBoxGroup<>();
//            checkBoxGroup.setItems(orgTags);
//            checkBoxGroup.setItemCaptionGenerator(OrgTag::getName);
//            checkBoxGroup.addBlurListener(event -> saveTags());
//
//            if (initialSelectedTagIds != null)
//            {
//                orgTags.stream()
//                    .filter(tag -> initialSelectedTagIds.contains(tag.getId()))
//                    .forEach(checkBoxGroup::select);
//            }
//
//            return checkBoxGroup;
//        }
//
//        void saveTags()
//        {
//            Set<OrgTag> selectedTags = new HashSet<>();
//            checkBoxGroups.forEach(group -> selectedTags.addAll(group.getSelectedItems()));
//
//            List<String> tagIds = selectedTags.stream()
//                .map(OrgTag::getId)
//                .collect(Collectors.toList());
//
//            eventMgr.setEventTagIds(tagIds, tagTypeName);
//            eventMgr.save();
//
//            msgDisplayer.displayMessage(descGenerator.plural() + " saved");
//            setOrgTagsDisclosureCaption();
//        }
//    }

}