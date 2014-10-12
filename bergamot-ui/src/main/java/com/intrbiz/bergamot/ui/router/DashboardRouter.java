package com.intrbiz.bergamot.ui.router;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

import static com.intrbiz.bergamot.ui.util.Sorter.*;

@Prefix("/")
@Template("layout/main")
@RequireValidPrincipal()
public class DashboardRouter extends Router<BergamotApp>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @SessionVar("site") Site site)
    {
        model("alerts", db.listChecksThatAreNotOk(site.getId()));
        model("groups", orderGroupsByStatus(db.getRootGroups(site.getId())));
        model("locations", orderLocationsByStatus(db.getRootLocations(site.getId())));
        encode("index");
    }
}
