package com.intrbiz.bergamot.ui.router;

import static com.intrbiz.bergamot.ui.util.Sorter.*;

import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
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
    public void index(BergamotDB db, @SessionVar("site") Site site)
    {
        model("alerts", db.listAlerts(site.getId()).stream().map(Alert::getCheck).filter((c) -> permission("read", c)).collect(Collectors.toList()));
        model("groups", orderGroupsByStatus(permission("read", db.getRootGroups(site.getId()))));
        model("locations", orderLocationsByStatus(permission("read", db.getRootLocations(site.getId()))));
        encode("index");
    }
}
