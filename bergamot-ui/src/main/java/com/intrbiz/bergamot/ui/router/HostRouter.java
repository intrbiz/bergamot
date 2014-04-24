package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.message.task.ExecuteCheck;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Template;

@Prefix("/host")
@Template("layout/main")
public class HostRouter extends Router
{
    private Bergamot getBergamot()
    {
        return ((BergamotApp) this.app()).getBergamot();
    }
    
    @Any("/name/:name")
    public void host(String name)
    {
        Bergamot bergamot = this.getBergamot();
        model("host", bergamot.getObjectStore().lookupHost(name));
        encode("host/detail");
    }
    
    @Any("/execute/:id")
    public void executeHost(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the host and force it's execution
        Host host = bergamot.getObjectStore().lookupHost(id);
        if (host != null)
        {
            // build the check and dispatch it
            ExecuteCheck executeCheck = host.createExecuteCheck();
            if (executeCheck != null) bergamot.getManifold().publish(executeCheck);
        }
        redirect("/host/name/" + host.getName());
    }
    
    @Any("/enable/:id")
    public void enableHost(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the service and enable it
        Host host = bergamot.getObjectStore().lookupHost(id);
        if (host != null)
        {
            // enable the service with the scheduler
            host.setEnabled(true);
            bergamot.getScheduler().enable(host);
        }
        redirect("/host/name/" + host.getName());
    }
    
    @Any("/disable/:id")
    public void disableHost(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the service and disable it
        Host host = bergamot.getObjectStore().lookupHost(id);
        if (host != null)
        {
            // disable the service with the scheduler
            host.setEnabled(false);
            bergamot.getScheduler().disable(host);
        }
        redirect("/host/name/" + host.getName());
    }
    
    @Any("/suppress/:id")
    public void suppressHost(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the service and supress it
        Host host = bergamot.getObjectStore().lookupHost(id);
        if (host != null)
        {
            // suppress the service
            host.setSuppressed(true);
        }
        redirect("/host/name/" + host.getName());
    }
    
    @Any("/unsuppress/:id")
    public void unsuppressHost(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the service and unsupress it
        Host host = bergamot.getObjectStore().lookupHost(id);
        if (host != null)
        {
            // unsuppress the service
            host.setSuppressed(false);
        }
        redirect("/host/name/" + host.getName());
    }
    
    @Any("/execute-services/:id")
    public void executeServicesOnHost(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the host and force it's execution
        Host host = bergamot.getObjectStore().lookupHost(id);
        if (host != null)
        {
            for (Service service : host.getServices())
            {
                // build the check and dispatch it
                ExecuteCheck executeCheck = service.createExecuteCheck();
                if (executeCheck != null) bergamot.getManifold().publish(executeCheck);
            }
        }
        redirect("/host/name/" + host.getName());
    }
}
