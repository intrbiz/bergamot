package com.intrbiz.bergamot.ui.router;

import java.io.IOException;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.bergamot.model.ServiceGroup;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Template;

import static com.intrbiz.bergamot.ui.util.Sorter.*;

@Prefix("/")
@Template("layout/main")
public class ServiceGroupsRouter extends Router
{
    private Bergamot getBergamot()
    {
        return ((BergamotApp) this.app()).getBergamot();
    }
    
    @Any("/servicegroup")
    public void rediectServiceGroups() throws IOException
    {
        redirect("/servicegroup/");
    }
    
    @Any("/servicegroup/")
    public void showServiceGroups()
    {
        Bergamot bergamot = this.getBergamot();
        model("servicegroups", orderServiceGroupsByStatus(bergamot.getObjectStore().getServicegroups()));
        encode("servicegroup/index");
    }
    
    @Any("/servicegroup/name/:name")
    public void showServiceGroupByName(String name)
    {
        Bergamot bergamot = this.getBergamot();
        ServiceGroup serviceGroup = model("servicegroup", bergamot.getObjectStore().lookupServicegroup(name));
        model("services", orderServicesByStatus(serviceGroup.getServices()));
        encode("servicegroup/services");
    }
}
