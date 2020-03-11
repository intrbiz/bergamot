package com.intrbiz.bergamot.ui.action;

import com.intrbiz.balsa.action.BalsaAction;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.event.site.DeinitSite;
import com.intrbiz.bergamot.model.message.event.site.InitSite;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Action;

public class SiteActions implements BalsaAction<BergamotApp>
{

    public SiteActions()
    {
        super();
    }

    @Action("site-init")
    public void initSite(Site site)
    {
        this.app().getSiteEventBroker().publish(new InitSite(site.getId(), site.getName()));
    }
    
    @Action("site-deinit")
    public void disableCheck(Site site)
    {
        this.app().getSiteEventBroker().publish(new DeinitSite(site.getId(), site.getName()));
    }
}
