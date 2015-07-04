package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/host")
@Template("layout/main")
@RequireValidPrincipal()
public class HostRouter extends Router<BergamotApp>
{   
    @Any("/name/:name")
    @WithDataAdapter(BergamotDB.class)
    public void host(BergamotDB db, String name, @SessionVar("site") Site site)
    {
        Host host = model("host", db.getHostByName(site.getId(), name));
        model("alerts", db.getAllAlertsForCheck(host.getId(), 3, 0));
        encode("host/detail");
    }
    
    @Any("/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void host(BergamotDB db, @IsaObjectId UUID id)
    {
        model("host", db.getHost(id));
        model("alerts", db.getAllAlertsForCheck(id, 3, 0));
        encode("host/detail");
    }
    
    @Any("/execute/:id")
    @WithDataAdapter(BergamotDB.class)
    public void executeHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Host host = db.getHost(id);
        if (host != null)
        {
            action("execute-check", host);
        }
        redirect("/host/id/" + id);
    }
    
    @Any("/enable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void enableHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Host host = db.getHost(id);
        if (host != null)
        {
            host.setEnabled(true);
            db.setHost(host);
            action("enable-check", host);
        }
        redirect("/host/id/" + id);
    }
    
    @Any("/disable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void disableHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Host host = db.getHost(id);
        if (host != null)
        {
            host.setEnabled(false);
            db.setHost(host);
            action("disable-check", host);
        }
        redirect("/host/id/" + id);
    }
    
    @Any("/suppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void suppressHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Host host = db.getHost(id);
        if (host != null)
        {
            host.setSuppressed(true);
            db.setHost(host);
        }
        redirect("/host/id/" + id);
    }
    
    @Any("/unsuppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void unsuppressHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Host host = db.getHost(id);
        if (host != null)
        {
            host.setSuppressed(false);
            db.setHost(host);
        }
        redirect("/host/id/" + id);
    }
    
    @Any("/execute-services/:id")
    @WithDataAdapter(BergamotDB.class)
    public void executeServicesOnHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        for (Service service : db.getServicesOnHost(id))
        {
            action("execute-check", service);
        }
        redirect("/host/id/" + id);
    }
    
    @Any("/suppress-all/:id")
    @WithDataAdapter(BergamotDB.class)
    public void suppressServicesOnHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        for (Service service : db.getServicesOnHost(id))
        {
            action("suppress-check", service);
        }
        for (Trap trap : db.getTrapsOnHost(id))
        {
            action("suppress-check", trap);
        }
        redirect("/host/id/" + id);
    }
    
    @Any("/unsuppress-all/:id")
    @WithDataAdapter(BergamotDB.class)
    public void unsuppressServicesOnHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        for (Service service : db.getServicesOnHost(id))
        {
            action("unsuppress-check", service);
        }
        for (Trap trap : db.getTrapsOnHost(id))
        {
            action("suppress-check", trap);
        }
        redirect("/host/id/" + id);
    }
}
