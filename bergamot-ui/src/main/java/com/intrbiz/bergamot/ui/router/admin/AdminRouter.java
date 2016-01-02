package com.intrbiz.bergamot.ui.router.admin;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.health.HealthTracker;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/admin")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
public class AdminRouter extends Router<BergamotApp>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db)
    {
        boolean showDaemons = var("showDaemons", ! "no".equalsIgnoreCase(System.getProperty("admin.show.daemons", "yes")));
        if (showDaemons) var("daemons", HealthTracker.getInstance().getDaemons());
        encode("admin/index");
    }
}
