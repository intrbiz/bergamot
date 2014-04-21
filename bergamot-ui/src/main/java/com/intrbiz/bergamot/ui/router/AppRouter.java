package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.task.Check;
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
    
    @Any("/service/execute/:name")
    public void execService(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the service and force it's execution
        Service service = bergamot.getObjectStore().lookupService(id);
        Check check = service.createCheck();
        if (check != null) bergamot.getManifold().publish(check);
        redirect("/service/" + id);
    }
}
