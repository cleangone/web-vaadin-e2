package xyz.cleangone.e2.web.vaadin.util;

import java.util.List;

public class DescriptionGenerator
{
    private final String descSingle;
    private final String descPlural;

    public DescriptionGenerator(String descSingle, String descPlural)
    {
        this.descSingle = descSingle;
        this.descPlural = descPlural;
    }

    public String plural()
    {
        return descPlural;
    }

    public String numText(List<? extends Object> list)
    {
        return list == null || list.isEmpty() ? "No" : list.size() + "";
    }

    public String text(List<? extends Object> list)
    {
        return list != null && list.size() == 1 ? descSingle : descPlural;
    }
}
