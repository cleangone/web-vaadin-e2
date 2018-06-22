package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.tag;

import com.vaadin.ui.Label;
import xyz.cleangone.data.aws.dynamo.entity.organization.TagType;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.AdminPageType;
import xyz.cleangone.e2.web.vaadin.desktop.admin.nav.BaseNavCol;

import java.util.ArrayList;
import java.util.List;

public class TagsNavCol extends BaseNavCol
{
    protected final TagsAdminLayout tagsAdminLayout;

    protected TagManager tagMgr;

    public TagsNavCol(TagsAdminLayout tagsAdminLayout)
    {
        this.tagsAdminLayout = tagsAdminLayout;
    }

    public void set(SessionManager sessionMgr)
    {
        tagMgr = sessionMgr.getOrgManager().getTagManager();
    }

    protected void addLinks()
    {
        String tagTypesStyle = tagsAdminLayout.getAdminPageType() == TagAdminPageType.TAG_TYPES ? STYLE_LINK_ACTIVE : STYLE_LINK;
        addComponent(getLink("Tag Types", tagTypesStyle, e -> {
            tagMgr.setTagType(null);
            setPage(TagAdminPageType.TAG_TYPES);
        }));

        int longestNameLength = TagAdminPageType.TAG_TYPES.toString().length();

        TagType currTagType = tagMgr.getTagType();
        List<TagType> tagTypes = new ArrayList<>(tagMgr.getTagTypes());
        for (TagType tagType : tagTypes)
        {
            longestNameLength = Math.max(longestNameLength, tagType.getName().length());
            if (currTagType != null && currTagType.getName().equals(tagType.getName()))
            {
                Label label = new Label(tagType.getName());
                label.setStyleName(STYLE_LINK_ACTIVE);
                addComponent(label);
            }
            else
            {
                addComponent(getLink(tagType.getName(), STYLE_LINK, e -> {
                    tagMgr.setTagType(tagType);
                    setPage(TagAdminPageType.TAG_TYPE);
                }));
            }
        }

        addSpacer(longestNameLength + 15);
    }

    protected void setPage(AdminPageType pageType)
    {
        tagsAdminLayout.setAdminPage(pageType);
    }
}
