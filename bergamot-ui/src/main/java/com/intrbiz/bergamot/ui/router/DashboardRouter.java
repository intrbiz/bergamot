package com.intrbiz.bergamot.ui.router;

import static com.intrbiz.bergamot.ui.util.Sorter.*;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.GetBergamotSite;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CurrentPrincipal;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/")
@Template("layout/main")
@RequireValidPrincipal()
public class DashboardRouter extends Router<BergamotUI>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @GetBergamotSite() Site site, @CurrentPrincipal() Contact contact)
    {
        model("alerts", db.listAlertsForContact(site.getId(), contact.getId()));
        model("groups", orderGroupsByStatus(permission("read", db.getRootGroups(site.getId()))));
        model("locations", orderLocationsByStatus(permission("read", db.getRootLocations(site.getId()))));
        encode("index");
    }
}
