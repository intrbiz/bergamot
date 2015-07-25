package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.model.message.HostMO;
import com.intrbiz.bergamot.model.message.ServiceMO;
import com.intrbiz.bergamot.model.message.TrapMO;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;


@Prefix("/api/host")
@RequireValidPrincipal()
public class HostAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @RequirePermission("api.read.host")
    @WithDataAdapter(BergamotDB.class)
    public List<HostMO> getHosts(BergamotDB db, @Var("site") Site site)
    {
        return db.listHosts(site.getId()).stream().map(Host::toStubMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.host")
    @WithDataAdapter(BergamotDB.class)
    public HostMO getHost(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getHostByName(site.getId(), name), Host::toMO);
    }
    
    @Get("/name/:name/state")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.host")
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getHostState(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getHostByName(site.getId(), name), (h)->{return h.getState().toMO();});
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.host")
    @WithDataAdapter(BergamotDB.class)
    public HostMO getHost(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getHost(id), Host::toMO);
    }
    
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.host")
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getHostState(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getHost(id), (h)->{return h.getState().toMO();});
    }
    
    @Get("/name/:name/services")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.host")
    @RequirePermission("api.read.service")
    @WithDataAdapter(BergamotDB.class)
    public List<ServiceMO> getHostServices(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getHostByName(site.getId(), name), (h) -> {return h.getServices().stream().map(Service::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/services")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.host")
    @RequirePermission("api.read.service")
    @WithDataAdapter(BergamotDB.class)
    public List<ServiceMO> getHostServices(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getHost(id), (e)->{return e.getServices().stream().map(Service::toMO).collect(Collectors.toList());});
    }
    
    @Get("/name/:name/traps")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.host")
    @RequirePermission("api.read.trap")
    @WithDataAdapter(BergamotDB.class)
    public List<TrapMO> getHostTraps(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getHostByName(site.getId(), name), (h)->{return h.getTraps().stream().map(Trap::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/traps")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.host")
    @RequirePermission("api.read.trap")
    @WithDataAdapter(BergamotDB.class)
    public List<TrapMO> getHostTraps(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getHost(id), (e)->{return e.getTraps().stream().map(Trap::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/execute")
    @JSON()
    @RequirePermission("api.write.host.execute")
    @WithDataAdapter(BergamotDB.class)
    public String executeHost(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Host host = db.getHost(id);
        if (host == null) throw new BalsaNotFound("No host with id '" + id + "' exists.");
        action("execute-check", host);
        return "Ok";
    }
    
    @Get("/id/:id/suppress")
    @JSON()
    @RequirePermission("api.write.host.suppress")
    @WithDataAdapter(BergamotDB.class)
    public String suppress(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Host host = db.getHost(id);
        if (host == null) throw new BalsaNotFound("No host with id '" + id + "' exists.");
        action("suppress-check", host);
        return "Ok";
    }
    
    @Get("/id/:id/unsuppress")
    @JSON()
    @RequirePermission("api.write.host.unsuppress")
    @WithDataAdapter(BergamotDB.class)
    public String unsuppress(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Host host = db.getHost(id);
        if (host == null) throw new BalsaNotFound("No host with id '" + id + "' exists.");
        action("unsuppress-check", host);
        return "Ok";
    }
    
    @Get("/id/:id/execute-services")
    @JSON()
    @RequirePermission("api.write.host.execute")
    @RequirePermission("api.write.service.execute")
    @WithDataAdapter(BergamotDB.class)
    public String executeServicesOnHost(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Host host = db.getHost(id);
        if (host == null) throw new BalsaNotFound("No host with id '" + id + "' exists.");
        int executed = 0;
        for (Service service : host.getServices())
        {
            action("execute-check", service);
            executed++;
        }
        return "Ok, executed " + executed + " services";
    }
    
    @Get("/id/:id/suppress-services")
    @JSON()
    @RequirePermission("api.write.host.suppress")
    @RequirePermission("api.write.service.suppress")
    @WithDataAdapter(BergamotDB.class)
    public String suppressServicesOnHost(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Host host = db.getHost(id);
        if (host == null) throw new BalsaNotFound("No host with id '" + id + "' exists.");
        int suppressed = 0;
        for (Service service : host.getServices())
        {
            action("suppress-check", service);
            suppressed++;
        }
        return "Ok, suppressed " + suppressed + " services";
    }
    
    @Get("/id/:id/unsuppress-services")
    @JSON()
    @RequirePermission("api.write.host.unsuppress")
    @RequirePermission("api.write.service.unsuppress")
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressServicesOnHost(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Host host = db.getHost(id);
        if (host == null) throw new BalsaNotFound("No host with id '" + id + "' exists.");
        int unsuppressed = 0;
        for (Service service : host.getServices())
        {
            action("unsuppress-check", service);
            unsuppressed++;
        }
        return "Ok, unsuppressed " + unsuppressed + " services";
    }
    
    @Get("/id/:id/suppress-traps")
    @JSON()
    @RequirePermission("api.write.host.suppress")
    @RequirePermission("api.write.trap.suppress")
    @WithDataAdapter(BergamotDB.class)
    public String suppressTrapsOnHost(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Host host = db.getHost(id);
        if (host == null) throw new BalsaNotFound("No host with id '" + id + "' exists.");
        int suppressed = 0;
        for (Trap trap : host.getTraps())
        {
            action("suppress-check", trap);
            suppressed++;
        }
        return "Ok, suppressed " + suppressed + " traps";
    }
    
    @Get("/id/:id/unsuppress-traps")
    @JSON()
    @RequirePermission("api.write.host.unsuppress")
    @RequirePermission("api.write.trap.unsuppress")
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressTrapsOnHost(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Host host = db.getHost(id);
        if (host == null) throw new BalsaNotFound("No host with id '" + id + "' exists.");
        int unsuppressed = 0;
        for (Trap trap : host.getTraps())
        {
            action("unsuppress-check", trap);
            unsuppressed++;
        }
        return "Ok, unsuppressed " + unsuppressed + " traps";
    }
    
    @Get("/id/:id/suppress-all")
    @JSON()
    @RequirePermission("api.write.host.suppress")
    @RequirePermission("api.write.service.suppress")
    @RequirePermission("api.write.trap.suppress")
    @WithDataAdapter(BergamotDB.class)
    public String suppressAllOnHost(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Host host = db.getHost(id);
        if (host == null) throw new BalsaNotFound("No host with id '" + id + "' exists.");
        int suppressed = 0;
        for (Service service : host.getServices())
        {
            action("suppress-check", service);
            suppressed++;
        }
        for (Trap trap : host.getTraps())
        {
            action("suppress-check", trap);
            suppressed++;
        }
        return "Ok, suppressed " + suppressed;
    }
    
    @Get("/id/:id/unsuppress-all")
    @JSON()
    @RequirePermission("api.write.host.unsuppress")
    @RequirePermission("api.write.service.unsuppress")
    @RequirePermission("api.write.trap.unsuppress")
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressAllOnHost(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Host host = db.getHost(id);
        if (host == null) throw new BalsaNotFound("No host with id '" + id + "' exists.");
        int unsuppressed = 0;
        for (Service service : host.getServices())
        {
            action("unsuppress-check", service);
            unsuppressed++;
        }
        for (Trap trap : host.getTraps())
        {
            action("unsuppress-check", trap);
            unsuppressed++;
        }
        return "Ok, unsuppressed " + unsuppressed;
    }
    
    @Get("/name/:name/config.xml")
    @XML(notFoundIfNull = true)
    @RequirePermission("api.read.host.config")
    @WithDataAdapter(BergamotDB.class)
    public HostCfg getHostConfig(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getHostByName(site.getId(), name), Host::getConfiguration);
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @RequirePermission("api.read.host.config")
    @WithDataAdapter(BergamotDB.class)
    public HostCfg getHostConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getHost(id), Host::getConfiguration);
    }
}
