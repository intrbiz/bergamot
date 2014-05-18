package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.message.task.check.ExecuteCheck;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Template;

@Prefix("/service")
@Template("layout/main")
public class ServiceRouter extends Router
{
    private Bergamot getBergamot()
    {
        return ((BergamotApp) this.app()).getBergamot();
    }
    
    @Any("/name/:host/:service")
    public void service(String hostName, String serviceName)
    {
        Bergamot bergamot = this.getBergamot();
        Host host = bergamot.getObjectStore().lookupHost(hostName);
        model("service", host.getService(serviceName));
        encode("service/detail");
    }
    
    @Any("/id/:id")
    public void service(@AsUUID UUID id)
    {
        Bergamot bergamot = this.getBergamot();
        model("service", bergamot.getObjectStore().lookupService(id));
        encode("service/detail");
    }
    
    @Any("/execute/:id")
    public void executeService(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the service and force it's execution
        Service service = bergamot.getObjectStore().lookupService(id);
        if (service != null)
        {
            // build the check and dispatch it
            ExecuteCheck executeCheck = service.executeCheck();
            if (executeCheck != null) bergamot.getManifold().publish(executeCheck);
        }
        redirect("/service/id/" + id);
    }
    
    @Any("/enable/:id")
    public void enableService(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the service and enable it
        Service service = bergamot.getObjectStore().lookupService(id);
        if (service != null)
        {
            // enable the service with the scheduler
            service.setEnabled(true);
            bergamot.getScheduler().enable(service);
        }
        redirect("/service/id/" + id);
    }
    
    @Any("/disable/:id")
    public void disableService(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the service and disable it
        Service service = bergamot.getObjectStore().lookupService(id);
        if (service != null)
        {
            // disable the service with the scheduler
            service.setEnabled(false);
            bergamot.getScheduler().disable(service);
        }
        redirect("/service/id/" + id);
    }
    
    @Any("/suppress/:id")
    public void suppressService(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the service and supress it
        Service service = bergamot.getObjectStore().lookupService(id);
        if (service != null)
        {
            // suppress the service
            service.setSuppressed(true);
            bergamot.getObjectStore().removeAlert(service);
        }
        redirect("/service/id/" + id);
    }
    
    @Any("/unsuppress/:id")
    public void unsuppressService(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the service and unsupress it
        Service service = bergamot.getObjectStore().lookupService(id);
        if (service != null)
        {
            // unsuppress the service
            service.setSuppressed(false);
        }
        redirect("/service/id/" + id);
    }
}
