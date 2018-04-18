package xyz.cleangone.e2.web.vaadin.desktop.admin.nav;

import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.*;
import xyz.cleangone.e2.web.vaadin.util.VaadinUtils;

public abstract class BaseNavCol extends VerticalLayout
{
    protected static String STYLE_ADMIN_NAV = "adminNav";
    protected static String STYLE_LINK = "link";
    protected static String STYLE_LINK_ACTIVE = "linkActive";
    protected static String STYLE_FONT_BOLD = "fontBold";
    protected static String STYLE_INDENT = "marginLeft";

    protected AdminPageType currPageType;

    public BaseNavCol()
    {
        setMargin(true);
        setSpacing(true);
        setWidthUndefined();
        setHeight((UI.getCurrent().getPage().getBrowserWindowHeight() - 100) + "px");

        setStyleName(STYLE_ADMIN_NAV);
    }

    public void setLinks(AdminPageType pageType)
    {
        currPageType = pageType;
        set();
    }

    public void set()
    {
        removeAllComponents();
        addLinks();
    }

    protected abstract void addLinks();
    protected abstract void setPage(AdminPageType pageType);

    protected void addSpacer(int size)
    {
        StringBuilder spacerName = new StringBuilder();
        for(int i=0; i<size; i++)
        {
            spacerName.append("&nbsp;");
        }
        Label spacer = VaadinUtils.getHtmlLabel(spacerName.toString());

        addComponent(spacer);
        setExpandRatio(spacer, 1.0f);
    }

    protected VerticalLayout getTightLayout()
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setMargin(false);

        return layout;
    }

    protected Component getLink(AdminPageType pageType)
    {
        String styleName = currPageType == pageType ? STYLE_LINK_ACTIVE : STYLE_LINK;
        return getLink(pageType.toString(), styleName, e -> setPage(pageType));
    }

    protected Component getLink(String text, String styleName, LayoutEvents.LayoutClickListener listener)
    {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setMargin(false);
        layout.addComponent(new Label(text));
        layout.addLayoutClickListener(listener);
        layout.setStyleName(styleName);

        return(layout);
    }
}
