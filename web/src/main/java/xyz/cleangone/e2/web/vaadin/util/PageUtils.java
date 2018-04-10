package xyz.cleangone.e2.web.vaadin.util;

import com.vaadin.server.Page;
import com.vaadin.ui.VerticalLayout;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.getOrDefault;

public class PageUtils
{
    private static int COL_MIN_HEIGHT = 700;

    public static PageDisplayType getPageDisplayType(PageDisplayType... types)
    {
        PageDisplayType combinedType = PageDisplayType.NotApplicable;
        for (PageDisplayType type : types)
        {
            combinedType = getPageDisplayType(combinedType, type);
        }

        return combinedType;
    }

    public static String setNavStyle(String styleNamePrefix, BaseOrg baseOrg)
    {
        String styleName = styleNamePrefix + baseOrg.getTag();

        Page.Styles styles = Page.getCurrent().getStyles();
        String backgroundColor = getOrDefault(baseOrg.getNavBackgroundColor(), "whitesmoke");
        styles.add("." + styleName + " {background: " + backgroundColor + ";  border-right: 1px solid silver;}");

        return styleName;
    }

    public static PageDisplayType getPageDisplayType(PageDisplayType type1, PageDisplayType type2)
    {
        if (type1 == type2) { return type1; }
        else if (type1 == PageDisplayType.NotApplicable) { return type2; }
        else if (type2 == PageDisplayType.NotApplicable) { return type1; }
        else if (type1 == PageDisplayType.NoChange) { return type2; }
        else if (type2 == PageDisplayType.NoChange) { return type1; }
        else { return type1; }
    }

    public static VerticalLayout getMarginLayout() { return getMarginLayout(COL_MIN_HEIGHT); }
    public static VerticalLayout getMarginLayout(int colHeight)
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setWidth("25px");
        layout.setHeight(colHeight + "px");

        return layout;
    }
}
