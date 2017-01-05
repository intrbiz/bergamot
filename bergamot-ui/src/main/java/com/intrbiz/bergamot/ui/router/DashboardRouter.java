package com.intrbiz.bergamot.ui.router;

import static com.intrbiz.bergamot.ui.util.Sorter.orderGroupsByStatus;
import static com.intrbiz.bergamot.ui.util.Sorter.orderLocationsByStatus;

import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;
import com.intrbiz.bergamot.metadata.GetBergamotSite;

@Prefix("/")
@Template("layout/main")
@RequireValidPrincipal()
public class DashboardRouter extends Router<BergamotApp>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @GetBergamotSite() Site site)
    {
        model("alerts", db.listAlerts(site.getId()).stream().filter((a) -> permission("read", a.getCheckId())).collect(Collectors.toList()));
        model("groups", orderGroupsByStatus(permission("read", db.getRootGroups(site.getId()))));
        model("locations", orderLocationsByStatus(permission("read", db.getRootLocations(site.getId()))));
        encode("index");
    }
}
