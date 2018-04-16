package xyz.cleangone.e2.web.vaadin.desktop.banner;

import com.vaadin.ui.Component;
import xyz.cleangone.e2.web.manager.SessionManager;

public interface BannerComponent extends Component
{
    void reset(SessionManager sessionMgr);
}
