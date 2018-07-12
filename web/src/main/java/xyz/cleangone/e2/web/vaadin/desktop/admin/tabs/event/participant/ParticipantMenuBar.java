package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.participant;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.BaseActionBar;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.BaseMenuBar;

import java.util.List;

public class ParticipantMenuBar extends BaseActionBar
{
    private final ParticipantsAdmin participantsAdmin;
    private MyLeftMenuBar leftMenuBar = new MyLeftMenuBar();
    private MyCenterMenuBar centerMenuBar = new MyCenterMenuBar();

    public ParticipantMenuBar(ParticipantsAdmin participantsAdmin)
    {
        this.participantsAdmin = participantsAdmin;

        HorizontalLayout leftLayout = getLayout(leftMenuBar, "40%");
        HorizontalLayout centerLayout = getLayout(centerMenuBar, "50%");

        addComponents(leftLayout, centerLayout);
    }

    public void setTagsForAddingParticipant(List<OrgTag> tags)
    {
        leftMenuBar.setTags(tags);
    }
    public void setTagsToAdd(List<OrgTag> tags)
    {
        centerMenuBar.setTagsToAdd(tags);
    }
    public void setTagsToRemove(List<OrgTag> tags)
    {
        centerMenuBar.setTagsToRemove(tags);
    }

    class MyLeftMenuBar extends BaseMenuBar
    {
        MenuItem addPeopleMenuItem;

        public MyLeftMenuBar()
        {
            MenuItem menuItem = addItem("",  VaadinIcons.PLUS, null);
            menuItem.setStyleName("icon-only");

            addPeopleMenuItem = menuItem.addItem("Add People with Tag", null, null);
        }

        void setTags(List<OrgTag> tags)
        {
            addPeopleMenuItem.removeChildren();

            if (tags != null)
            {
                for (OrgTag tag : tags)
                {
                    addPeopleMenuItem.addItem(tag.getName(), null, new Command() {
                        public void menuSelected(MenuItem selectedItem) {
                            participantsAdmin.addPeopleWithTag(tag);
                        }
                    });
                }
            }
        }
    }

    class MyCenterMenuBar extends MenuBar
    {
        MenuItem addTagItem;
        MenuItem removeTagItem;

        public MyCenterMenuBar()
        {
            addStyleName(ValoTheme.MENUBAR_BORDERLESS);

            MenuItem menuItem = addItem("",  VaadinIcons.MENU, null);
            menuItem.setStyleName("icon-only");

            addTagItem = menuItem.addItem("Add Tag", null, null);
            removeTagItem = menuItem.addItem("Remove Tag", null, null);

            addTagItem.setEnabled(false);
            removeTagItem.setEnabled(false);
        }

        void setTagsToAdd(List<OrgTag> tags)
        {
            resetMenuItem(addTagItem, tags);
            if (tags != null)
            {
                for (OrgTag tag : tags)
                {
                    addTagItem.addItem(tag.getName(), null, new Command() {
                        public void menuSelected(MenuItem selectedItem) { participantsAdmin.addTagToSelectedPeople(tag); }
                    });
                }
            }
        }

        void setTagsToRemove(List<OrgTag> tags)
        {
            resetMenuItem(removeTagItem, tags);
            if (tags != null)
            {
                for (OrgTag tag : tags)
                {
                    removeTagItem.addItem(tag.getName(), null, new Command() {
                        public void menuSelected(MenuItem selectedItem)
                        {
                            participantsAdmin.removeTagFromSelectedPeople(tag);
                        }
                    });
                }
            }
        }

        void resetMenuItem(MenuItem menuItem, List<OrgTag> tags)
        {
            menuItem.removeChildren();
            menuItem.setEnabled (tags != null && !tags.isEmpty());
        }
    }

}
