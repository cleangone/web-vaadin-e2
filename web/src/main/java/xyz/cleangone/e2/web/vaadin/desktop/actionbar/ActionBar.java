package xyz.cleangone.e2.web.vaadin.desktop.actionbar;

import com.vaadin.server.Page;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.organization.BaseOrg;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgEvent;
import xyz.cleangone.data.aws.dynamo.entity.organization.Organization;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.desktop.org.PageDisplayType;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;
import xyz.cleangone.e2.web.vaadin.util.PageUtils;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class ActionBar extends BaseActionBar implements MessageDisplayer
{
    private LeftMenuBar leftMenuBar = new LeftMenuBar();
    private CenterMenuBar centerMenuBar = new CenterMenuBar();
    private RightMenuBar rightMenuBar = new RightMenuBar();

    public ActionBar()
    {
        // todo - may need to adjust thes %'s on the fly to adapt to mobile
        HorizontalLayout leftLayout = getLayout(leftMenuBar, "10%");
        HorizontalLayout centerLayout = getLayout(centerMenuBar, "50%");
        HorizontalLayout rightLayout = getLayout(rightMenuBar, "40%");
        rightLayout.addComponent(getHtmlLabel(""));

        addComponents(leftLayout, centerLayout, rightLayout);
        setComponentAlignment(rightLayout, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));
    }

    public PageDisplayType set(SessionManager sessionMgr)
    {
        setStyle(sessionMgr);
        PageDisplayType leftDisplayType   = leftMenuBar.set(sessionMgr);
        PageDisplayType centerDisplayType = centerMenuBar.set(sessionMgr);
        PageDisplayType rightDisplayType  = rightMenuBar.set(sessionMgr);

        return PageUtils.getPageDisplayType(leftDisplayType, centerDisplayType, rightDisplayType);
    }

    public void displayMessage(String msg)
    {
        centerMenuBar.displayMessage(msg);
    }
    public void setCartMenuItem()
    {
        rightMenuBar.setCartMenuItem();
    }

    public MenuBar.MenuItem override(String caption)
    {
        leftMenuBar.removeItems();
        centerMenuBar.removeItems();
        rightMenuBar.removeItems();

        return leftMenuBar.addItem(caption, null, null);
    }
}
