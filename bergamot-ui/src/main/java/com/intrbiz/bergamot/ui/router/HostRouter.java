package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Post;
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
        Host host = model("host", notNull(db.getHostByName(site.getId(), name)));
        require(permission("read", host));
        model("alerts", db.getAllAlertsForCheck(host.getId(), 3, 0));
        encode("host/detail");
    }
    
    @Any("/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void host(BergamotDB db, @IsaObjectId UUID id)
    {
        Host host = model("host", notNull(db.getHost(id)));
        require(permission("read", host));
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
            if (permission("execute", host)) action("execute-check", host);
        }
        redirect("/host/id/" + id);
    }
    
    @Any("/enable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void enableHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Host host = notNull(db.getHost(id));
        require(permission("enable", host));
        // enable the host
        host.setEnabled(true);
        db.setHost(host);
        // update scheduling
        action("enable-check", host);
        redirect("/host/id/" + id);
    }
    
    @Any("/disable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void disableHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Host host = notNull(db.getHost(id));
        require(permission("disable", host));
        // disable the host
        host.setEnabled(false);
        db.setHost(host);
        // update scheduler
        action("disable-check", host);
        redirect("/host/id/" + id);
    }
    
    @Any("/suppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void suppressHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Host host = notNull(db.getHost(id));
        require(permission("suppress", host));
        // suppress the host
        host.setSuppressed(true);
        db.setHost(host);
        redirect("/host/id/" + id);
    }
    
    @Any("/unsuppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void unsuppressHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Host host = notNull(db.getHost(id));
        require(permission("unsuppress", host));
        // unsuppress the host
        host.setSuppressed(false);
        db.setHost(host);
        redirect("/host/id/" + id);
    }
    
    @Any("/execute-services/:id")
    @WithDataAdapter(BergamotDB.class)
    public void executeServicesOnHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        for (Service service : db.getServicesOnHost(id))
        {
            if (permission("execute", service)) action("execute-check", service);
        }
        redirect("/host/id/" + id);
    }
    
    @Any("/suppress-all/:id")
    @WithDataAdapter(BergamotDB.class)
    public void suppressServicesOnHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        for (Service service : db.getServicesOnHost(id))
        {
            if (permission("suppress", service)) action("suppress-check", service);
        }
        for (Trap trap : db.getTrapsOnHost(id))
        {
            if (permission("suppress", trap)) action("suppress-check", trap);
        }
        redirect("/host/id/" + id);
    }
    
    @Any("/unsuppress-all/:id")
    @WithDataAdapter(BergamotDB.class)
    public void unsuppressServicesOnHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        for (Service service : db.getServicesOnHost(id))
        {
            if (permission("unsuppress", service)) action("unsuppress-check", service);
        }
        for (Trap trap : db.getTrapsOnHost(id))
        {
            if (permission("unsuppress", trap)) action("suppress-check", trap);
        }
        redirect("/host/id/" + id);
    }
    
    @Get("/create")
    @WithDataAdapter(BergamotDB.class)
    public void create(BergamotDB db, @SessionVar("site") Site site)
    {
        var("templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(HostCfg.class)).stream().filter((t) -> permission("read", t.getId())).sorted((a, b) -> a.getSummary().compareTo(b.getSummary())).collect(Collectors.toList()));
        var("locations", db.listLocations(site.getId()).stream().filter((l) -> permission("read", l)).sorted((a, b) -> a.getSummary().compareTo(b.getSummary())).collect(Collectors.toList()));
        var("groups", db.listGroups(site.getId()).stream().filter((g) -> permission("read", g)).sorted((a, b) -> a.getSummary().compareTo(b.getSummary())).collect(Collectors.toList()));
        encode("/host/create");
    }
    
    @Post("/create")
    @WithDataAdapter(BergamotDB.class)
    public void create(BergamotDB db)
    {
        
    }
}
