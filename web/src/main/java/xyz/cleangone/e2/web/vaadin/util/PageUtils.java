package xyz.cleangone.e2.web.vaadin.util;

import com.vaadin.server.Page;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class PageUtils
{
    public static int DEFAULT_MARGIN_WIDTH = 10;

    public static ZoneId TIME_ZONE_ID = ZoneId.of("America/Los_Angeles");

    public static SimpleDateFormat SDF_ADMIN = new SimpleDateFormat("EEE MMM d, h:mmaaa z");
    public static SimpleDateFormat SDF_ADMIN_GRID = new SimpleDateFormat("MM/dd h:mmaaa");
    public static SimpleDateFormat SDF_THIS_WEEK = new SimpleDateFormat("EEEE h:mmaaa z");
    public static SimpleDateFormat SDF_NEXT_WEEK = new SimpleDateFormat("EEE MMM d, h:mmaaa z");
    static
    {
        SDF_ADMIN.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID));
        SDF_ADMIN_GRID.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID));
        SDF_THIS_WEEK.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID));
        SDF_NEXT_WEEK.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID));
    }


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

    public static VerticalLayout getMarginLayout(UI ui, int bannerHeight)
    {
        VerticalLayout margin = getMarginLayout(ui.getPage().getBrowserWindowHeight() - bannerHeight);
        ui.getPage().addBrowserWindowResizeListener(e ->
            margin.setHeight(getPx(ui.getPage().getBrowserWindowHeight() - bannerHeight))
        );

        return margin;
    }

    public static VerticalLayout getMarginLayout(int colHeight) { return getMarginLayout(colHeight, DEFAULT_MARGIN_WIDTH); }
    public static VerticalLayout getMarginLayout(int colHeight, int marginWidth)
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setWidth(getPx(marginWidth));
        layout.setHeight(getPx(colHeight));

        return layout;
    }

    public static LocalDateTime getLocalDateTime(Date date)
    {
        if (date == null) { return null; }
        return date.toInstant()
            .atZone(TIME_ZONE_ID)
            .toLocalDateTime();
    }

    public static Date getDate(DateTimeField dateField)
    {
        return getDate(dateField.getValue());
    }
    public static Date getDate(LocalDateTime date)
    {
        if (date == null) { return null; }
        return java.util.Date
            .from(date.atZone(TIME_ZONE_ID).toInstant());
    }

    public static String getPx(int i)
    {
        return i + "px";
    }
}
