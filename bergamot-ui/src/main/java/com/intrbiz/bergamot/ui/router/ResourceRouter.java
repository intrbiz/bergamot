package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Template;

@Prefix("/resource")
@Template("layout/main")
public class ResourceRouter extends Router
{
    private Bergamot getBergamot()
    {
        return ((BergamotApp) this.app()).getBergamot();
    }
    
    @Any("/name/:host/:resource")
    public void resource(String hostName, String resourceName)
    {
        Bergamot bergamot = this.getBergamot();
        Cluster cluster = bergamot.getObjectStore().lookupCluster(hostName);
        model("resource", cluster.getResource(resourceName));
        encode("resource/detail");
    }
    
    @Any("/id/:id")
    public void resource(@AsUUID UUID id)
    {
        Bergamot bergamot = this.getBergamot();
        model("resource", bergamot.getObjectStore().lookupResource(id));
        encode("resource/detail");
    }
    
    @Any("/enable/:id")
    public void enableResource(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the resource and enable it
        Resource resource = bergamot.getObjectStore().lookupResource(id);
        if (resource != null)
        {
            resource.setEnabled(true);
        }
        redirect("/resource/id/" + id);
    }
    
    @Any("/disable/:id")
    public void disableResource(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the resource and disable it
        Resource resource = bergamot.getObjectStore().lookupResource(id);
        if (resource != null)
        {
            resource.setEnabled(false);
        }
        redirect("/resource/id/" + id);
    }
    
    @Any("/suppress/:id")
    public void suppressResource(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the resource and supress it
        Resource resource = bergamot.getObjectStore().lookupResource(id);
        if (resource != null)
        {
            // suppress the resource
            resource.setSuppressed(true);
            bergamot.getObjectStore().removeAlert(resource);
        }
        redirect("/resource/id/" + id);
    }
    
    @Any("/unsuppress/:id")
    public void unsuppressResource(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the resource and unsupress it
        Resource resource = bergamot.getObjectStore().lookupResource(id);
        if (resource != null)
        {
            // unsuppress the resource
            resource.setSuppressed(false);
        }
        redirect("/resource/id/" + id);
    }
}
