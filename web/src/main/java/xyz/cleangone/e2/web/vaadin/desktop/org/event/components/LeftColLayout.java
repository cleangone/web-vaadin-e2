package xyz.cleangone.e2.web.vaadin.desktop.org.event.components;

import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.OrgManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.data.manager.UserManager;
import xyz.cleangone.data.manager.event.ItemManager;
import xyz.cleangone.e2.web.manager.EntityChangeManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.CatalogPage;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventPage;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xyz.cleangone.e2.web.vaadin.util.PageUtils.*;

public class LeftColLayout extends HorizontalLayout
{
    protected final VerticalLayout leftLayout = new VerticalLayout();

    private OrgManager orgMgr;
    protected EventManager eventMgr;
    protected ItemManager itemMgr;
    protected UserManager userMgr;
    protected TagManager tagMgr;
    protected OrgEvent event;
    protected User user;
    private EntityChangeManager changeManager = new EntityChangeManager();

    public LeftColLayout(int pageHeight)
    {
        setMargin(false);
        setSpacing(false);
        addComponents(getMarginLayout(pageHeight), leftLayout);

        leftLayout.setSpacing(false);
        leftLayout.setWidthUndefined();
        leftLayout.setMargin(new MarginInfo(true, true, true, false)); // T/R/B/L margins
    }

    public void set(SessionManager sessionMgr)
    {
        orgMgr = sessionMgr.getOrgManager();
        eventMgr = sessionMgr.getEventManager();
        itemMgr = eventMgr.getItemManager();
        userMgr = sessionMgr.getUserManager();
        tagMgr = orgMgr.getTagManager();

        event = eventMgr.getEvent();
        user = userMgr.getUser();
    }

    public PageDisplayType set() { return set((OrgTag)null); }
    public PageDisplayType set(OrgTag currentCategory)
    {
        if (changeManager.unchanged(user) &&
            changeManager.unchanged(event) &&
            changeManager.unchanged(orgMgr.getOrgId(), EntityType.Category) &&
            changeManager.unchanged(event, EntityType.Entity, EntityType.Category))
        {
            return PageDisplayType.NoChange;
        }

        if (changeManager.changed(event) || changeManager.changed(event, EntityType.Entity))
        {
            setStyle();
        }

        changeManager.reset(user, event);
        removeAllComponents();
        List<String> categoryIds = event.getCategoryIds();
        if (categoryIds == null || categoryIds.isEmpty()) { return PageDisplayType.NoRetrieval; }

        List<OrgTag> categories = tagMgr.getTags(categoryIds);
        if (!categories.isEmpty())
        {
            leftLayout.removeAllComponents();
            leftLayout.addComponent(getCategoriesLayout(currentCategory, categories));

            addComponents(getMarginLayout(), leftLayout);
        }

        return PageDisplayType.ObjectRetrieval;
    }

    private Component getCategoriesLayout(OrgTag currentCategory, List<OrgTag> categories)
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);

        String textColor = VaadinUtils.getOrDefault(event.getNavTextColor(), "black");
        String selectedTextColor = VaadinUtils.getOrDefault(event.getNavSelectedTextColor(), "black");

        Page.Styles styles = Page.getCurrent().getStyles();

        // todo - these styles need to be -org-event
        String textStyleName = "category-text-" + event.getTag();
        styles.add("." + textStyleName + " {color: " + textColor + "}");

        String selectedTextStyleName = "category-text-selected" + event.getTag();
        styles.add("." + selectedTextStyleName + " {color: " + selectedTextColor + "}");

        layout.addComponent(getEventLink(textStyleName));

        Map<String, Integer> itemCountByCategoryId = new HashMap<>();
        List<CatalogItem> items = itemMgr.getItems();
        for (CatalogItem item : items)
        {
            for (String categoryId : item.getCategoryIds())
            {
                int count = itemCountByCategoryId.getOrDefault(categoryId, 0);
                itemCountByCategoryId.put(categoryId, count + 1);
            }
        }

        for (OrgTag category : categories)
        {
            layout.addComponent(getCategoryLink(
                category, itemCountByCategoryId.getOrDefault(category.getId(), 0), currentCategory, textStyleName, selectedTextStyleName));
        }

        return layout;
    }

    private Component getEventLink(String textStyleName)
    {
        HorizontalLayout layout = getLink(event.getName(), textStyleName);
        layout.addLayoutClickListener( e -> {
            String viewName = EventPage.NAME + "-" + event.getTag();
            getUI().getNavigator().addView(viewName, new EventPage());
            getUI().getNavigator().navigateTo(viewName);
        });

        return layout;
    }

    private Component getCategoryLink(
        OrgTag category, long itemCount, OrgTag currentCategory, String textStyleName, String selectedTextStyleName)
    {
        String styleName = currentCategory != null && currentCategory.getId().equals(category.getId()) ? selectedTextStyleName: textStyleName;
        HorizontalLayout layout = getLink(category.getName() + " (" + itemCount + ")", styleName);
        layout.addLayoutClickListener( e -> {
            eventMgr.setCategory(category);
            String viewName = CatalogPage.NAME + "-" + category.getName();
            getUI().getNavigator().addView(viewName, new CatalogPage());
            getUI().getNavigator().navigateTo(viewName);
        });

        return layout;
    }

    private HorizontalLayout getLink(String text, String textStyleName)
    {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setMargin(false);

        Label label = new Label(text);
        label.addStyleName("category");
        label.addStyleName(textStyleName);

        layout.addComponent(label);
        return layout;
    }

    private void setStyle()
    {
        setStyleName(setNavStyle("menu-left-" + orgMgr.getOrg().getTag() + "-", event));
    }
}