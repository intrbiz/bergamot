package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
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
        model("cluster", db.getClusterByName(site.getId(), name));
        encode("cluster/detail");
    }
    
    @Any("/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void showClusterById(BergamotDB db, @AsUUID UUID id)
    {
        model("cluster", db.getCheck(id));
        encode("cluster/detail");
    }
    
    @Any("/enable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void enableCluster(BergamotDB db, @AsUUID UUID id) throws IOException
    {
        Cluster cluster = db.getCluster(id);
        if (cluster != null)
        {
            cluster.setEnabled(true);
            db.setCluster(cluster);
        }
        redirect("/cluster/id/" + id);
    }
    
    @Any("/disable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void disableCluster(BergamotDB db, @AsUUID UUID id) throws IOException
    {
        Cluster cluster = db.getCluster(id);
        if (cluster != null)
        {
            cluster.setEnabled(false);
            db.setCluster(cluster);
        }
        redirect("/cluster/id/" + id);
    }
    
    @Any("/suppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void suppressCluster(BergamotDB db, @AsUUID UUID id) throws IOException
    {
        Cluster cluster = db.getCluster(id);
        if (cluster != null)
        {
            cluster.setSuppressed(true);
            db.setCluster(cluster);
        }
        redirect("/cluster/id/" + id);
    }
    
    @Any("/unsuppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void unsuppressCluster(BergamotDB db, @AsUUID UUID id) throws IOException
    {
        Cluster cluster = db.getCluster(id);
        if (cluster != null)
        {
            cluster.setSuppressed(false);
            db.setCluster(cluster);
        }
        redirect("/cluster/id/" + id);
    }
}
