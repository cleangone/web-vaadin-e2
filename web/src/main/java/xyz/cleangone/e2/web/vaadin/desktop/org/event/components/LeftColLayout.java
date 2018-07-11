package xyz.cleangone.e2.web.vaadin.desktop.org.event.components;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.item.SaleStatus;
import xyz.cleangone.data.aws.dynamo.entity.lastTouched.EntityType;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.organization.TagType;
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xyz.cleangone.e2.web.vaadin.util.PageUtils.*;
import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class LeftColLayout extends HorizontalLayout
{
    protected final VerticalLayout leftLayout = vertical(MARGIN_TR, SPACING_FALSE, WIDTH_UNDEFINED);

    private int pageWidth;
    private VerticalLayout marginLayout;
    private SessionManager sessionMgr;
    private OrgManager orgMgr;
    protected EventManager eventMgr;
    protected ItemManager itemMgr;
    protected UserManager userMgr;
    protected TagManager tagMgr;
    protected OrgEvent event;
    protected User user;
    private EntityChangeManager changeManager = new EntityChangeManager();

    OrgTag currentCategory;
    List<OrgTag> categories;
    Map<String, Integer> itemCountByCategoryId;

    private boolean colMinimized = false;

    public LeftColLayout(int pageHeight, int pageWidth)
    {
        this.pageWidth = pageWidth;

        // horizontal to hold margin layout, which ensures col background color for height of page
        setMargin(false);
        setSpacing(false);

        marginLayout = getMarginLayout(pageHeight);
        addComponents(marginLayout, leftLayout);
    }

    public void set(SessionManager sessionMgr)
    {
        this.sessionMgr = sessionMgr;
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
        this.currentCategory = currentCategory;

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

        categories = tagMgr.getEventVisibleTags(TagType.CATEGORY_TAG_TYPE, event);
        if (categories.isEmpty()) { return PageDisplayType.ObjectRetrieval; }

        itemCountByCategoryId = new HashMap<>();
        List<CatalogItem> items = itemMgr.getItems();
        for (CatalogItem item : items)
        {
            if (item.getEnabled() &&
                item.getSaleStatus() != SaleStatus.Preview &&
                (item.getAvailabilityStart() == null || item.getAvailabilityStart().before(new Date())))
            {
                for (String categoryId : item.getCategoryIds())
                {
                    int count = itemCountByCategoryId.getOrDefault(categoryId, 0);
                    itemCountByCategoryId.put(categoryId, count + 1);
                }
            }
        }

        if (sessionMgr.isMobileBrowser() && pageWidth < 500)
        {
            colMinimized = true;
            setLayout(leftLayout, MARGIN_FALSE);
            marginLayout.setWidth(getPx(0));
        }

        leftLayout.removeAllComponents();

        if (colMinimized) { leftLayout.addComponent(new CategoryMenu()); }
        else { leftLayout.addComponent(getCategoriesLayout()); }

        addComponents(marginLayout, leftLayout);

        return PageDisplayType.ObjectRetrieval;
    }

    public int getColWidth()
    {
        return colMinimized ? 50 : 200;
    }

    private Component getCategoriesLayout()
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

        for (OrgTag category : categories)
        {
            int itemCount = itemCountByCategoryId.getOrDefault(category.getId(), 0);
            if (itemCount != 0)
            {
                layout.addComponent(getCategoryLink(category, itemCount, currentCategory, textStyleName, selectedTextStyleName));
            }
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
        layout.addLayoutClickListener(e -> navigateTo(category));

        return layout;
    }

    private HorizontalLayout getLink(String text, String textStyleName)
    {
        Label label = new Label(text);
        label.addStyleName("category");
        label.addStyleName(textStyleName);

        return horizontal(label, MARGIN_FALSE);
    }

    private void navigateTo(OrgTag category)
    {
        eventMgr.setCategory(category);
        String viewName = CatalogPage.NAME + "-" + category.getName();
        getUI().getNavigator().addView(viewName, new CatalogPage());
        getUI().getNavigator().navigateTo(viewName);
    }

    private void setStyle()
    {
        setStyleName(setNavStyle("menu-left-" + orgMgr.getOrg().getTag() + "-", event));
    }

    private class CategoryMenu extends MenuBar
    {
        CategoryMenu()
        {
            addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            setMargin(false);

            MenuBar.MenuItem categoryItem = addItem("", null, null);
            categoryItem.setIcon(VaadinIcons.LIST_UL);

            for (OrgTag category : categories)
            {
                int itemCount = itemCountByCategoryId.getOrDefault(category.getId(), 0);
                if (itemCount != 0)
                {
                    categoryItem.addItem(category.getName(), null, new MenuBar.Command() {
                        public void menuSelected(MenuBar.MenuItem selectedItem) {
                            navigateTo(category);
                        }
                    });
                }
            }
        }
    }
}