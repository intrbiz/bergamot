package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Template;

@Prefix("/cluster")
@Template("layout/main")
public class ClusterRouter extends Router<BergamotApp>
{    
    @Any("/name/:name")
    public void cluster(String name)
    {
        Bergamot bergamot = this.app().getBergamot();
        model("cluster", bergamot.getObjectStore().lookupCluster(name));
        encode("cluster/detail");
    }
    
    @Any("/id/:id")
    public void cluster(@AsUUID UUID id)
    {
        Bergamot bergamot = this.app().getBergamot();
        model("cluster", bergamot.getObjectStore().lookupCluster(id));
        encode("cluster/detail");
    }
    
    @Any("/enable/:id")
    public void enableCluster(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.app().getBergamot();
        // get the service and enable it
        Cluster cluster = bergamot.getObjectStore().lookupCluster(id);
        if (cluster != null)
        {
            // enable the service with the scheduler
            cluster.setEnabled(true);
        }
        redirect("/cluster/name/" + cluster.getName());
    }
    
    @Any("/disable/:id")
    public void disableCluster(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.app().getBergamot();
        // get the service and disable it
        Cluster cluster = bergamot.getObjectStore().lookupCluster(id);
        if (cluster != null)
        {
            // disable the service with the scheduler
            cluster.setEnabled(false);
        }
        redirect("/cluster/name/" + cluster.getName());
    }
    
    @Any("/suppress/:id")
    public void suppressCluster(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.app().getBergamot();
        // get the service and supress it
        Cluster cluster = bergamot.getObjectStore().lookupCluster(id);
        if (cluster != null)
        {
            // suppress the service
            cluster.setSuppressed(true);
        }
        redirect("/cluster/name/" + cluster.getName());
    }
    
    @Any("/unsuppress/:id")
    public void unsuppressCluster(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.app().getBergamot();
        // get the service and unsupress it
        Cluster cluster = bergamot.getObjectStore().lookupCluster(id);
        if (cluster != null)
        {
            // unsuppress the service
            cluster.setSuppressed(false);
        }
        redirect("/cluster/name/" + cluster.getName());
    }
}
