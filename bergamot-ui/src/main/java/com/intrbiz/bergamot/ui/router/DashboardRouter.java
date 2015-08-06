package com.intrbiz.bergamot.ui.router;

import static com.intrbiz.bergamot.ui.util.Sorter.*;

import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CurrentPrincipal;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/")
@Template("layout/main")
@RequireValidPrincipal()
public class DashboardRouter extends Router<BergamotApp>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @SessionVar("site") Site site, @CurrentPrincipal Contact user)
    {
        model("alerts", user.hasPermission("read", db.listAlerts(site.getId()).stream().map(Alert::getCheck).collect(Collectors.toList())));
        model("groups", orderGroupsByStatus(user.hasPermission("read", db.getRootGroups(site.getId()))));
        model("locations", orderLocationsByStatus(user.hasPermission("read", db.getRootLocations(site.getId()))));
        encode("index");
    }
}
