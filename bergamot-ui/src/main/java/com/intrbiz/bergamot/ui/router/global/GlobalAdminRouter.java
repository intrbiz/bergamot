package com.intrbiz.bergamot.ui.router.global;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.health.HealthTracker;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CurrentPrincipal;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/global/admin")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
public class GlobalAdminRouter extends Router<BergamotApp>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @CurrentPrincipal Contact principal)
    {
        boolean globalAdmin = var("globalAdmin", principal.isGlobalAdmin());
        require(globalAdmin);
        boolean showDaemons = var("showDaemons", ! "no".equalsIgnoreCase(System.getProperty("admin.show.daemons", "yes")));
        if (showDaemons) var("daemons", HealthTracker.getInstance().getDaemons());
        if (globalAdmin)
        {
            // list sites
            var("sites", db.listSites());
        }
        encode("/global/admin/index");
    }
}
