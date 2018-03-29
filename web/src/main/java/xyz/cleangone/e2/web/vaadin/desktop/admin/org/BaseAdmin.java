package xyz.cleangone.e2.web.vaadin.desktop.admin.org;

import com.vaadin.ui.*;
import xyz.cleangone.e2.web.manager.SessionManager;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

public abstract class BaseAdmin extends VerticalLayout
{
    protected final MessageDisplayer msgDisplayer;
    protected UI ui;

    public BaseAdmin(MessageDisplayer msgDisplayer)
    {
        this.msgDisplayer = msgDisplayer;
    }

    public abstract void set(SessionManager sessionMgr);

    public void set(SessionManager sessionMgr, UI ui)
    {
        this.ui = ui;
        set(sessionMgr);
    }

    public abstract void set();
}
