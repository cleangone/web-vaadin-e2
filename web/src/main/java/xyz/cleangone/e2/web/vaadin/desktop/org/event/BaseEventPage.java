package xyz.cleangone.e2.web.vaadin.desktop.org.event;

import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.data.manager.*;
import xyz.cleangone.data.manager.event.ItemManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.BasePage;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.components.RightColLayout;
import xyz.cleangone.e2.web.vaadin.util.PageUtils;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

import java.util.*;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.getOrDefault;

public abstract class BaseEventPage extends BasePage implements View
{
    static int COL_MIN_HEIGHT = 700;

    protected enum PageCols { Left, Center, Right };

    private final HorizontalLayout leftWrapper = new HorizontalLayout();
    protected final VerticalLayout leftLayout = new VerticalLayout();
    protected final VerticalLayout centerLayout = new VerticalLayout();
    protected final RightColLayout rightLayout;

    protected EventManager eventMgr;
    protected ItemManager itemMgr;
    protected UserManager userMgr;
    protected TagManager tagMgr;

    protected OrgEvent event;
    protected User user;

    public BaseEventPage()
    {
        this(PageCols.Left, PageCols.Center, PageCols.Right);
    }

    public BaseEventPage(PageCols... pageCols)
    {
        super(new HorizontalLayout(), BannerStyle.Single);  // mainLayout is horizontal
        mainLayout.setWidth("100%");
        mainLayout.setHeightUndefined();
        mainLayout.setMargin(false);

        leftWrapper.setMargin(false);
        leftWrapper.setSpacing(false);
        leftWrapper.addComponents(getMarginLayout(), leftLayout);

        leftLayout.setSpacing(false);
        leftLayout.setWidthUndefined();
        leftLayout.setMargin(new MarginInfo(true, true, true, false)); // T/R/B/L margins

        centerLayout.setMargin(new MarginInfo(false, true, false, true)); // T/R/B/L margins

        rightLayout = new RightColLayout(actionBar);

        for (PageCols pageCol : pageCols)
        {
            if (pageCol == PageCols.Left) { mainLayout.addComponent(leftWrapper); }
            else if (pageCol == PageCols.Center)
            {
                mainLayout.addComponent(centerLayout);
                mainLayout.setExpandRatio(centerLayout, 1.0f);
            }
            else if (pageCol == PageCols.Right) { mainLayout.addComponent(rightLayout); }
        }
    }

    protected VerticalLayout getMarginLayout()
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setWidth("25px");
        layout.setHeight(COL_MIN_HEIGHT + "px");

        return layout;
    }

    protected PageDisplayType set(SessionManager sessionMgr)
    {
        super.set(sessionMgr);
        eventMgr = sessionMgr.getEventManager();
        itemMgr = eventMgr.getItemManager();
        userMgr = sessionMgr.getUserManager();
        tagMgr = orgMgr.getTagManager();

        event = eventMgr.getEvent();
        user = userMgr.getUser();

        rightLayout.set(sessionMgr);

        resetHeader();
        return PageDisplayType.NotApplicable;
    }

    protected PageDisplayType setLeftLayout() { return setLeftLayout(null); }
    protected PageDisplayType setLeftLayout(OrgTag currentCategory)
    {
        leftWrapper.removeAllComponents();
        List<String> categoryIds = event.getCategoryIds();
        if (categoryIds == null || categoryIds.isEmpty()) { return PageDisplayType.NoRetrieval; }

        Date retrievalDate = new Date();
        List<OrgTag> categories = tagMgr.getTags(categoryIds);
        if (!categories.isEmpty())
        {
            leftLayout.removeAllComponents();
            leftLayout.addComponent(getCategoriesLayout(currentCategory, categories));

            leftWrapper.addComponents(getMarginLayout(), leftLayout);
        }

        return PageDisplayType.ObjectRetrieval;
    }

    protected Component getCategoriesLayout(OrgTag currentCategory, List<OrgTag> categories)
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

    protected Component getEventLink(String textStyleName)
    {
        HorizontalLayout layout = getLink(event.getName(), textStyleName);
        layout.addLayoutClickListener( e -> {
            String viewName = EventPage.NAME + "-" + event.getTag();
            getUI().getNavigator().addView(viewName, new EventPage());
            getUI().getNavigator().navigateTo(viewName);
        });

        return layout;
    }

    protected Component getCategoryLink(
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

    protected HorizontalLayout getLink(String text, String textStyleName)
    {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setMargin(false);

        Label label = new Label(text);
        label.addStyleName("category");
        label.addStyleName(textStyleName);

        layout.addComponent(label);
        return layout;
    }

    protected void setMenuLeftStyle()
    {
        String styleName = "menu-left-" + event.getTag();

        Page.Styles styles = Page.getCurrent().getStyles();
        String backgroundColor = getOrDefault(event.getNavBackgroundColor(), "silver");
        styles.add("." + styleName + " {background: " + backgroundColor + ";  border-right: 1px solid silver;}");

        leftWrapper.setStyleName(styleName);
    }
}