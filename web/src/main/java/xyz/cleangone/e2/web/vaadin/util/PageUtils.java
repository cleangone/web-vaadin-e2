package xyz.cleangone.e2.web.vaadin.util;

import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;

import java.util.Collection;
import java.util.Date;
import java.util.List;


public class PageUtils
{
    public static PageDisplayType getPageDisplayType(PageDisplayType... types)
    {
        PageDisplayType combinedType = PageDisplayType.NotApplicable;
        for (PageDisplayType type : types)
        {
            combinedType = getPageDisplayType(combinedType, type);
        }

        return combinedType;
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
}
