package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/service")
@Template("layout/main")
@RequireValidPrincipal()
public class ServiceRouter extends Router<BergamotApp>
{
    @Any("/name/:host/:service")
    @WithDataAdapter(BergamotDB.class)
    public void service(BergamotDB db, String hostName, String serviceName, @SessionVar("site") Site site)
    {
        Service service = model("service", db.getServiceOnHostByName(site.getId(), hostName, serviceName));
        model("alerts", db.getAllAlertsForCheck(service.getId()));
        encode("service/detail");
    }
    
    @Any("/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void service(BergamotDB db, @AsUUID UUID id)
    {
        model("service", db.getService(id));
        model("alerts", db.getAllAlertsForCheck(id));
        encode("service/detail");
    }
    
    @Any("/execute/:id")
    @WithDataAdapter(BergamotDB.class)
    public void executeService(BergamotDB db, @AsUUID UUID id) throws IOException
    {
        Service service = db.getService(id);
        if (service != null)
        {
            action("execute-check", service);
        }
        redirect("/service/id/" + id);
    }
    
    @Any("/enable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void enableService(BergamotDB db, @AsUUID UUID id) throws IOException
    {
        Service service = db.getService(id);
        if (service != null)
        {
            service.setEnabled(true);
            db.setService(service);
            action("enable-check", service);
        }
        redirect("/service/id/" + id);
    }
    
    @Any("/disable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void disableService(BergamotDB db, @AsUUID UUID id) throws IOException
    {
        Service service = db.getService(id);
        if (service != null)
        {
            service.setEnabled(false);
            db.setService(service);
            action("disable-check", service);
        }
        redirect("/service/id/" + id);
    }
    
    @Any("/suppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void suppressService(BergamotDB db, @AsUUID UUID id) throws IOException
    {
        Service service = db.getService(id);
        if (service != null)
        {
            service.setSuppressed(true);
            db.setService(service);
        }
        redirect("/service/id/" + id);
    }
    
    @Any("/unsuppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void unsuppressService(BergamotDB db, @AsUUID UUID id) throws IOException
    {
        Service service = db.getService(id);
        if (service != null)
        {
            service.setSuppressed(false);
            db.setService(service);
        }
        redirect("/service/id/" + id);
    }
}
