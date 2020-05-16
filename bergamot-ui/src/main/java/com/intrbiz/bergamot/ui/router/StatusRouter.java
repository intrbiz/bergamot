package com.intrbiz.bergamot.ui.router;

import static com.intrbiz.balsa.BalsaContext.*;

import java.util.Date;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Template;

@Prefix("/")
@Template("layout/status")
public class StatusRouter extends Router<BergamotUI>
{
    @Any("/status/")
    @WithDataAdapter(BergamotDB.class)
    public void showStatusPages(BergamotDB db)
    {
        this.showStatusPage(db);
    }
    
    @Any("/status")
    @WithDataAdapter(BergamotDB.class)
    public void showStatusPage(BergamotDB db)
    {   
        // resolve the site using the host
        Site site = var("site", db.getSiteByName(Balsa().request().getServerName()));
        var(
            "groups", 
            db.listGroups(site.getId()).stream()
             .filter((g) -> "public".equals(g.getParameter("status-page")))
             .collect(Collectors.toList())
        );
        var("now", new Date());
        // render top level status page
        encode("status/index");
    }
    
    @Any("/status/:name")
    @WithDataAdapter(BergamotDB.class)
    public void showStatusPage(BergamotDB db, String name)
    {
        // resolve the site using the host
        Site site = var("site", db.getSiteByName(Balsa().request().getServerName()));
        // look up the group
        Group group = var("group", notNull(db.getGroupByName(site.getId(), name)));
        require("public".equals(group.getParameter("status-page")));
        var("now", new Date());
        // render the status page
        encode("status/group");
    }
}
