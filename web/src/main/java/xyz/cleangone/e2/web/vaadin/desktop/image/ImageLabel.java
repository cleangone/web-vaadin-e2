package xyz.cleangone.e2.web.vaadin.desktop.image;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Label;
import com.vaadin.ui.dnd.DragSourceExtension;

import java.awt.*;

public class ImageLabel extends Label
{
    private String url;

    public ImageLabel(String url) { this(url, ImageDimension.height(100)); }

    public ImageLabel(String url, ImageDimension dimension)
    {
        super("", ContentMode.HTML);
        this.url = url;

        new DragSourceExtension<>(this); // todo - should we always set this?

        setValue("<img src=" + url + " " + dimension.getHtml() + "/>");
    }

    public ImageLabel withHref()
    {
        setValue("<a href=" + url + " target=_blank/>" + getValue() + "<a/>");
        return this;
    }

    public String getUrl()
    {
        return url;
    }
}
