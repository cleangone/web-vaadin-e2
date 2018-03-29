package xyz.cleangone.e2.web.vaadin.desktop.banner;

import com.vaadin.server.Page;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import org.vaadin.alump.labelbutton.LabelButton;
import org.vaadin.alump.labelbutton.LabelButtonStyles;
import org.vaadin.alump.labelbutton.LabelClickListener;
import xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.OrgPage;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class BannerUtil
{
    public static String getBannerWidth()
    {
       return "100%";
    }
    public static String getBannerHeight()
    {
        return "250px";
    }

    public static AbsoluteLayout getBanner(BaseOrg baseOrg)
    {
        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setSizeFull();

        String url = baseOrg.getBannerUrl() == null ? "" : " url('" + baseOrg.getBannerUrl() + "') ";
        String backgroundColor = getOrDefault(baseOrg.getBannerBackgroundColor(), "gray");

        Page.Styles styles = Page.getCurrent().getStyles();
        String styleName = "banner-" + baseOrg.getTag();
        styles.add("." + styleName + " {background: " + backgroundColor + " " + url + " no-repeat center;}");
        layout.addStyleName(styleName);

        return layout;
    }

    public static Component getHtml(Organization org, UI ui)
    {
        return getLabelButton(org, e -> ui.getNavigator().navigateTo(OrgPage.NAME));
    }

    public static Component getHtml(OrgEvent event, SessionManager sessionMgr, UI ui)
    {
        return getLabelButton(event, e -> sessionMgr.navigateTo(event, ui.getNavigator()));
    }

    private static LabelButton getLabelButton(BaseOrg baseOrg, LabelClickListener listener)
    {
        String bannerHtml = baseOrg.getBannerHtml() == null ? baseOrg.getName() : baseOrg.getBannerHtml();

        String textColor = getOrDefault(baseOrg.getBannerTextColor(), "white");
        String textSize = getOrDefault(baseOrg.getBannerTextSize(), "30");

        String shadowColor = baseOrg.getBannerTextDropshadowColor();
        String textShadow = shadowColor == null ? "" : "text-shadow: 3px 3px " + shadowColor + ";";

        LabelButton labelButton = new LabelButton(
            "<div style=\"" + textShadow + " color:" + textColor + ";font-size:" + textSize + "px\"><b>" + bannerHtml + "</b></div>");
        labelButton.setContentMode(ContentMode.HTML);
        labelButton.addStyleName(LabelButtonStyles.POINTER_WHEN_CLICKABLE);

        labelButton.addLabelClickListener(listener);

        return labelButton;
    }

    public static void addComponentToLayout(Component component, AbsoluteLayout layout)
    {
        if (component != null && layout != null)
        {
            layout.addComponent(component, "left: 75px; bottom: 20px;");
        }
    }

}
