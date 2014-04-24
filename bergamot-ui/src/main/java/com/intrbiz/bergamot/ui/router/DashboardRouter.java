package com.intrbiz.bergamot.ui.router;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Template;

@Prefix("/")
@Template("layout/main")
public class DashboardRouter extends Router
{
    private Bergamot getBergamot()
    {
        return ((BergamotApp) this.app()).getBergamot();
    }
    
    @Any("/")
    public void index()
    {
        Bergamot bergamot = this.getBergamot();
        model("alerts", bergamot.getObjectStore().getAlerts());
        encode("index");
    }
}
