package xyz.cleangone.e2.web.vaadin.desktop.banner;

import com.vaadin.server.Page;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.alump.labelbutton.LabelButton;
import org.vaadin.alump.labelbutton.LabelButtonStyles;
import org.vaadin.alump.labelbutton.LabelClickListener;
import xyz.cleangone.data.aws.AwsClientFactory;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;
import xyz.cleangone.data.aws.dynamo.entity.organization.BannerText;
import xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.data.manager.ImageManager;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.OrgPage;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;
import static xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg.*;

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
        return getBanner(baseOrg, false);
    }
    public static AbsoluteLayout getBanner(BaseOrg baseOrg, boolean isMobileBrowser)
    {
        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setSizeFull();

        String backgroundColor = getOrDefault(baseOrg.getBannerBackgroundColor(), "gray");
        String url = baseOrg.getBannerUrl() == null ? "" : " url('" + baseOrg.getBannerUrl() + "') ";
        String gradientUrl = " url('" + ImageManager.getGradientUrl() + "') ";

        Page.Styles styles = Page.getCurrent().getStyles();
        String styleName = "banner-" + baseOrg.getTag();

        String xPos = isMobileBrowser ? getMobilePosition(baseOrg, BANNER_MOBILE_OFFSET_X_FIELD) : "50%";
        String yPos = isMobileBrowser ? getMobilePosition(baseOrg, BANNER_MOBILE_OFFSET_Y_FIELD) : "50%";

        String backgroundGradient = gradientUrl + " repeat center";
        String backgroundImage = backgroundColor + " " + url + " no-repeat " + xPos + " " + yPos;

        styles.add("." + styleName + " {background: " + backgroundGradient + ", " + backgroundImage + ";}");
        layout.addStyleName(styleName);

        return layout;
    }

    public static String getMobilePosition(BaseOrg baseOrg, EntityField field)
    {
        String offset = baseOrg.get(field);
        if (StringUtils.isBlank(offset)) { return "50%"; }
        else if (offset.trim().startsWith("-") || offset.trim().startsWith("")) { return "calc(50% " + offset + "px)"; }
        else return "calc(50% + " +  offset + "px)";
    }


    public static Component getHtml(Organization org, boolean isMobileBrowser, UI ui)
    {
        return getLabelButton(org, isMobileBrowser, e -> ui.getNavigator().navigateTo(OrgPage.NAME));
    }

    public static Component getHtml(OrgEvent event, SessionManager sessionMgr)
    {
        return getLabelButton(event, sessionMgr.isMobileBrowser());
    }

    public static Component getHtml(OrgEvent event, SessionManager sessionMgr, UI ui)
    {
        return getLabelButton(event, sessionMgr.isMobileBrowser(),  e -> sessionMgr.navigateTo(event, ui.getNavigator()));
    }

    private static LabelButton getLabelButton(BaseOrg baseOrg, boolean isMobileBrowser)
    {
        BannerText bannerText = isMobileBrowser ? baseOrg.getBannerTextMobile() : baseOrg.getBannerText();
        String bannerHtml = bannerText.getHtml() == null ? baseOrg.getName() : bannerText.getHtml();
        String textColor = getOrDefault(baseOrg.getBannerTextColor(), "white");
        String textSize   = getOrDefault(bannerText.getSize(), "30");
        String lineHeight = getOrDefault(bannerText.getLineHeight(), "140%");

        String div =  "<div style=\"color: " + textColor + "; " +
            "font-family: Times, serif; " +
            "line-height:" + lineHeight + "; " +
            "font-size: " + textSize + "px\">" +
            "<b>" + bannerHtml + "</b></div>";

        LabelButton labelButton = new LabelButton(div);
        labelButton.setContentMode(ContentMode.HTML);

        return labelButton;
    }

    private static LabelButton getLabelButton(BaseOrg baseOrg, boolean isMobileBrowser, LabelClickListener listener)
    {
        LabelButton labelButton = getLabelButton(baseOrg, isMobileBrowser);
        labelButton.addStyleName(LabelButtonStyles.POINTER_WHEN_CLICKABLE);
        labelButton.addLabelClickListener(listener);

        return labelButton;
    }

    public static void addComponentToLayout(Component component, AbsoluteLayout layout)
    {
        addComponentToLayout(component, layout, false);
    }

    public static void addComponentToLayout(Component component, AbsoluteLayout layout, boolean isMobile)
    {
        int leftOffset = isMobile ? 10 : 75;

        if (component != null && layout != null)
        {
            // todo -configurable
             layout.addComponent(component, "bottom:5px;left:" + leftOffset + "px");  // bottom was 20
        }
    }



}
