package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
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
import com.intrbiz.metadata.ListOf;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;
import com.intrbiz.metadata.doc.Desc;
import com.intrbiz.metadata.doc.Title;


@Title("Host API Methods")
@Desc({
    "Hosts represent pysical and virtual servers upon which Services run.  These API calls provide information on configured Host checks and their current state."
})
@Prefix("/api/host")
@RequireValidPrincipal()
public class HostAPIRouter extends Router<BergamotApp>
{
    @Title("List hosts")
    @Desc({
        "Retreive the list of all hosts for this site, with minimal information for each host."
    })
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @ListOf(HostMO.class)
    public List<HostMO> getHosts(BergamotDB db, @Var("site") Site site)
    {
        return db.listHosts(site.getId()).stream().map((x) -> x.toStubMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Title("Get host")
    @Desc({
        "Get the host check for the given name, returning minimal information about the host check."
    })
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public HostMO getHostByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Host host = notNull(db.getHostByName(site.getId(), name));
        require(permission("read", host));
        return host.toMO(currentPrincipal());
    }
    
    @Title("Get host state")
    @Desc({
        "Get the state of the host check for the given name, returning just the check state."
    })
    @Get("/name/:name/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getHostStateByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Host host = notNull(db.getHostByName(site.getId(), name));
        require(permission("read", host));
        return host.getState().toMO(currentPrincipal());
    }
    
    @Title("Get host")
    @Desc({
        "Get the host check for the given id (UUID), returning minimal information about the host check."
    })
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public HostMO getHost(BergamotDB db, @IsaObjectId() UUID id)
    {
        Host host = notNull(db.getHost(id));
        require(permission("read", host));
        return host.toMO(currentPrincipal());
    }
    
    @Title("Get host state")
    @Desc({
        "Get the state of the host check for the given id (UUID), returning just the check state."
    })
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getHostState(BergamotDB db, @IsaObjectId() UUID id)
    {
        Host host = notNull(db.getHost(id));
        require(permission("read", host));
        return host.getState().toMO(currentPrincipal());
    }
    
    @Title("Get services on host")
    @Desc({
        "Get all the services on the host identified by the given host name."
    })
    @Get("/name/:name/services")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(ServiceMO.class)
    public List<ServiceMO> getHostServicesByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Host host = notNull(db.getHostByName(site.getId(), name));
        require(permission("read", host));
        return host.getServices().stream().filter((x) -> permission("read", x)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Title("Get services on host")
    @Desc({
        "Get all the services on the host identified by the given UUID."
    })
    @Get("/id/:id/services")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(ServiceMO.class)
    public List<ServiceMO> getHostServices(BergamotDB db, @IsaObjectId() UUID id)
    {
        Host host = notNull(db.getHost(id));
        require(permission("read", host));
        return host.getServices().stream().filter((x) -> permission("read", x)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Title("Get traps on host")
    @Desc({
        "Get all the traps on the host identified by the given host name."
    })
    @Get("/name/:name/traps")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(TrapMO.class)
    public List<TrapMO> getHostTrapsByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Host host = notNull(db.getHostByName(site.getId(), name));
        require(permission("read", host));
        return host.getTraps().stream().filter((x) -> permission("read", x)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Title("Get traps on host")
    @Desc({
        "Get all the traps on the host identified by the given UUID."
    })
    @Get("/id/:id/traps")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(TrapMO.class)
    public List<TrapMO> getHostTraps(BergamotDB db, @IsaObjectId() UUID id)
    {
        Host host = notNull(db.getHost(id));
        require(permission("read", host));
        return host.getTraps().stream().filter((x) -> permission("read", x)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Title("Execute a host check")
    @Desc({
        "Execute a check immediately for the host identified by the given UUID."
    })
    @Get("/id/:id/execute")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String executeHost(BergamotDB db, @IsaObjectId() UUID id)
    { 
        Host host = notNull(db.getHost(id));
        require(permission("execute", host));
        action("execute-check", host);
        return "Ok";
    }
    
    @Title("Suppress a host")
    @Desc({
        "Suppress the host identified by the given UUID, this will prevent any alerts being raised for this host."
    })
    @Get("/id/:id/suppress")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String suppress(BergamotDB db, @IsaObjectId() UUID id)
    { 
        Host host = notNull(db.getHost(id));
        require(permission("suppress", host));
        action("suppress-check", host);
        return "Ok";
    }
    
    @Title("Unsuppress a host")
    @Desc({
        "Unsuppress the host identified by the given UUID, this will stop preventing any alerts being raised for this host."
    })
    @Get("/id/:id/unsuppress")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String unsuppress(BergamotDB db, @IsaObjectId() UUID id)
    { 
        Host host = notNull(db.getHost(id));
        require(permission("unsuppress", host));
        action("unsuppress-check", host);
        return "Ok";
    }
    
    @Title("Execute all service checks on a host")
    @Desc({
        "Execute a check immediately for every service on the host identified by the given UUID."
    })
    @Get("/id/:id/execute-services")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String executeServicesOnHost(BergamotDB db, @IsaObjectId() UUID id)
    { 
        Host host = notNull(db.getHost(id));
        int executed = 0;
        for (Service service : host.getServices())
        {
            if (permission("execute", service))
            {
                action("execute-check", service);
                executed++;
            }
        }
        return "Ok, executed " + executed + " services";
    }
    
    @Title("Suppress all services on a host")
    @Desc({
        "Suppress all services on the host identified by the given UUID."
    })
    @Get("/id/:id/suppress-services")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String suppressServicesOnHost(BergamotDB db, @IsaObjectId() UUID id)
    { 
        Host host = notNull(db.getHost(id));
        int suppressed = 0;
        for (Service service : host.getServices())
        {
            if (permission("suppress", service))
            {
                action("suppress-check", service);
                suppressed++;
            }
        }
        return "Ok, suppressed " + suppressed + " services";
    }
    
    @Title("Unsuppress all services on a host")
    @Desc({
        "Unsuppress all services on the host identified by the given UUID."
    })
    @Get("/id/:id/unsuppress-services")
    @JSON()
    @RequirePermission("api.write.host.unsuppress")
    @RequirePermission("api.write.service.unsuppress")
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressServicesOnHost(BergamotDB db, @IsaObjectId() UUID id)
    { 
        Host host = notNull(db.getHost(id));
        int unsuppressed = 0;
        for (Service service : host.getServices())
        {
            if (permission("unsuppress", service))
            {
                action("unsuppress-check", service);
                unsuppressed++;
            }
        }
        return "Ok, unsuppressed " + unsuppressed + " services";
    }
    
    @Title("Suppress all traps on a host")
    @Desc({
        "Suppress all traps on the host identified by the given UUID."
    })
    @Get("/id/:id/suppress-traps")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String suppressTrapsOnHost(BergamotDB db, @IsaObjectId() UUID id)
    { 
        Host host = notNull(db.getHost(id));
        int suppressed = 0;
        for (Trap trap : host.getTraps())
        {
            if (permission("suppress", trap))
            {
                action("suppress-check", trap);
                suppressed++;
            }
        }
        return "Ok, suppressed " + suppressed + " traps";
    }
    
    @Title("Unsuppress all traps on a host")
    @Desc({
        "Unsuppress all traps on the host identified by the given UUID."
    })
    @Get("/id/:id/unsuppress-traps")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressTrapsOnHost(BergamotDB db, @IsaObjectId() UUID id)
    { 
        Host host = notNull(db.getHost(id));
        int unsuppressed = 0;
        for (Trap trap : host.getTraps())
        {
            if (permission("unsuppress", trap))
            {
                action("unsuppress-check", trap);
                unsuppressed++;
            }
        }
        return "Ok, unsuppressed " + unsuppressed + " traps";
    }
    
    @Title("Suppress all services and traps on a host")
    @Desc({
        "Suppress all services and traps on the host identified by the given UUID."
    })
    @Get("/id/:id/suppress-all")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String suppressAllOnHost(BergamotDB db, @IsaObjectId() UUID id)
    { 
        Host host = notNull(db.getHost(id));
        int suppressed = 0;
        for (Service service : host.getServices())
        {
            if (permission("suppress", service))
            {
                action("suppress-check", service);
                suppressed++;
            }
        }
        for (Trap trap : host.getTraps())
        {
            if (permission("suppress", trap))
            {
                action("suppress-check", trap);
                suppressed++;
            }
        }
        return "Ok, suppressed " + suppressed;
    }
    
    @Title("Unsuppress all services and traps on a host")
    @Desc({
        "Unsuppress all services and traps on the host identified by the given UUID."
    })
    @Get("/id/:id/unsuppress-all")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressAllOnHost(BergamotDB db, @IsaObjectId() UUID id)
    { 
        Host host = notNull(db.getHost(id));
        int unsuppressed = 0;
        for (Service service : host.getServices())
        {
            if (permission("unsuppress", service))
            {
                action("unsuppress-check", service);
                unsuppressed++;
            }
        }
        for (Trap trap : host.getTraps())
        {
            if (permission("unsuppress", trap))
            {
                action("unsuppress-check", trap);
                unsuppressed++;
            }
        }
        return "Ok, unsuppressed " + unsuppressed;
    }
    
    @Title("Get host configuration")
    @Desc({
        "Get the configuration for the host identified by the given name, returning the XML configuration snippet."
    })
    @Get("/name/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public HostCfg getHostConfigByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Host host = notNull(db.getHostByName(site.getId(), name));
        require(permission("read.config", host));
        return host.getConfiguration();
    }
    
    @Title("Get host configuration")
    @Desc({
        "Get the configuration for the host identified by the given UUID, returning the XML configuration snippet."
    })
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public HostCfg getHostConfig(BergamotDB db, @IsaObjectId() UUID id)
    {
        Host host = notNull(db.getHost(id));
        require(permission("read.config", host));
        return host.getConfiguration();
    }
}
