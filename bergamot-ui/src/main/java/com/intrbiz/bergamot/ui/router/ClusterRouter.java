package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/cluster")
@Template("layout/main")
@RequireValidPrincipal()
public class ClusterRouter extends Router<BergamotApp>
{    
    @Any("/name/:name")
    @WithDataAdapter(BergamotDB.class)
    public void showClusterByName(BergamotDB db, String name, @SessionVar("site") Site site)
    {
        Cluster cluster = model("cluster", notNull(db.getClusterByName(site.getId(), name)));
        require(permission("read", cluster));
        model("alerts", db.getAllAlertsForCheck(cluster.getId(), 3, 0));
        encode("cluster/detail");
    }
    
    @Any("/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void showClusterById(BergamotDB db, @IsaObjectId UUID id)
    {
        Cluster cluster = model("cluster", notNull(db.getCluster(id)));
        require(permission("read", cluster));
        model("alerts", db.getAllAlertsForCheck(id, 3, 0));
        encode("cluster/detail");
    }
    
    @Any("/enable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void enableCluster(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Cluster cluster = notNull(db.getCluster(id));
        require(permission("enable", cluster));
        cluster.setEnabled(true);
        db.setCluster(cluster);
        redirect("/cluster/id/" + id);
    }
    
    @Any("/disable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void disableCluster(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Cluster cluster = notNull(db.getCluster(id));
        require(permission("disable", cluster));
        cluster.setEnabled(false);
        db.setCluster(cluster);
        redirect("/cluster/id/" + id);
    }
    
    @Any("/suppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void suppressCluster(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Cluster cluster = notNull(db.getCluster(id));
        require(permission("suppress", cluster));
        action("suppress-check", cluster);
        redirect("/cluster/id/" + id);
    }
    
    @Any("/unsuppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void unsuppressCluster(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Cluster cluster = notNull(db.getCluster(id));
        require(permission("unsuppress", cluster));
        action("unsuppress-check", cluster);
        redirect("/cluster/id/" + id);
    }
    
    @Any("/suppress-resource/:id")
    @WithDataAdapter(BergamotDB.class)
    public void suppressServicesOnHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        for (Resource resource : db.getResourcesOnCluster(id))
        {
            if (permission("suppress", resource)) action("suppress-check", resource);
        }
        redirect("/cluster/id/" + id);
    }
    
    @Any("/unsuppress-resource/:id")
    @WithDataAdapter(BergamotDB.class)
    public void unsuppressServicesOnHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        for (Resource resource : db.getResourcesOnCluster(id))
        {
            if (permission("unsuppress", resource)) action("unsuppress-check", resource);
        }
        redirect("/cluster/id/" + id);
    }
}
