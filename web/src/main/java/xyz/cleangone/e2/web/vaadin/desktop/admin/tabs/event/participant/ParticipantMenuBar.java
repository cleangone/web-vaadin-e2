package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.participant;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.BaseActionBar;
import xyz.cleangone.e2.web.vaadin.desktop.actionbar.BaseMenuBar;

import java.util.List;

public class ParticipantMenuBar extends BaseActionBar
{
    private final ParticipantsAdmin participantsAdmin;
    private MyLeftMenuBar leftMenuBar;
    private MyCenterMenuBar centerMenuBar = new MyCenterMenuBar();

    public ParticipantMenuBar(ParticipantsAdmin participantsAdmin)
    {
        this.participantsAdmin = participantsAdmin;
        leftMenuBar = new MyLeftMenuBar(participantsAdmin);

        HorizontalLayout leftLayout = getLayout(leftMenuBar, "40%");
        HorizontalLayout centerLayout = getLayout(centerMenuBar, "50%");

        PopupView popup = leftMenuBar.getPopup();
        leftLayout.addComponent(popup);
        leftLayout.setComponentAlignment(popup, new Alignment(AlignmentInfo.Bits.ALIGNMENT_BOTTOM));

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
//        MenuItem  addParticipantsItem;
        ComboBox<OrgTag> tagComboBox = new ComboBox<>();
        PopupView addParticipantsPopup;

        public MyLeftMenuBar(ParticipantsAdmin participantsAdmin)
        {
            MenuItem menuItem = addItem("",  VaadinIcons.PLUS, null);
            menuItem.setStyleName("icon-only");

            addParticipantsPopup = new PopupView(null, createPopupLayout());
            menuItem.addItem("Add Participants", null, new Command() {
                public void menuSelected(MenuItem selectedItem)
                {
                    addParticipantsPopup.setPopupVisible(true);
                }
            });
        }

        private Component createPopupLayout()
        {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setMargin(true);
            layout.setSpacing(true);
            layout.setSizeUndefined();

            tagComboBox.addStyleName(ValoTheme.TEXTFIELD_TINY);
            tagComboBox.setPlaceholder("Tag");
            tagComboBox.setItemCaptionGenerator(OrgTag::getName);

            Button button = new Button("Add People with Tag");
            button.addStyleName(ValoTheme.TEXTFIELD_TINY);
            button.addClickListener(new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    OrgTag tag = tagComboBox.getValue();
                    if (tag != null) { participantsAdmin.addPeopleWithTag(tag); }
                }
            });

            layout.addComponents(tagComboBox, button);
            return layout;
        }

        void setTags(List<OrgTag> tags)
        {
            tagComboBox.setItems(tags);
        }

        PopupView getPopup()
        {
            return addParticipantsPopup;
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
