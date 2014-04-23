package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.message.task.ExecuteCheck;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Template;

@Prefix("/")
@Template("layout/main")
public class AppRouter extends Router
{
    private Bergamot getBergamot()
    {
        return ((BergamotApp) this.app()).getBergamot();
    }
    
    @Any("/")
    public void index()
    {
        Bergamot bergamot = this.getBergamot();
        model("recent", bergamot.getObjectStore().getRecentChecks());
        encode("index");
    }
    
    @Any("/hosts")
    public void hosts()
    {
        Bergamot bergamot = this.getBergamot();
        model("hostgroups", bergamot.getObjectStore().getHostgroups());
        encode("host/index");
    }
    
    @Any("/host/:name")
    public void host(String name)
    {
        Bergamot bergamot = this.getBergamot();
        model("host", bergamot.getObjectStore().lookupHost(name));
        encode("host/detail");
    }
    
    @Any("/services")
    public void services()
    {
        Bergamot bergamot = this.getBergamot();
        model("servicegroups", bergamot.getObjectStore().getServicegroups());
        encode("service/index");
    }
    
    @Any("/service/:name")
    public void service(@AsUUID UUID id)
    {
        Bergamot bergamot = this.getBergamot();
        model("service", bergamot.getObjectStore().lookupService(id));
        encode("service/detail");
    }
    
    @Any("/service/execute/:id")
    public void executeService(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the service and force it's execution
        Service service = bergamot.getObjectStore().lookupService(id);
        if (service != null)
        {
            // build the check and dispatch it
            ExecuteCheck executeCheck = service.createCheck();
            if (executeCheck != null) bergamot.getManifold().publish(executeCheck);
        }
        redirect("/service/" + id);
    }
    
    @Any("/service/enable/:id")
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
        redirect("/service/" + id);
    }
    
    @Any("/service/disable/:id")
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
        redirect("/service/" + id);
    }
    
    @Any("/service/suppress/:id")
    public void suppressService(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the service and supress it
        Service service = bergamot.getObjectStore().lookupService(id);
        if (service != null)
        {
            // suppress the service
            service.setSuppressed(true);
        }
        redirect("/service/" + id);
    }
    
    @Any("/service/unsuppress/:id")
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
        redirect("/service/" + id);
    }
}
