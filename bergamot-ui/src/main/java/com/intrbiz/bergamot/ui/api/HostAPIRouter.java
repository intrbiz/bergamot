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
    "Hosts represent pysical and virtual servers upon which Services run.  These API calls provide information opn configured Host checks and their current state."
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
    
    @Title("Get host by name")
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
    
    @Title("Get host state by name")
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
    
    @Title("Get host by id")
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
    
    @Title("Get host state by name")
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
    
    @Get("/name/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public HostCfg getHostConfigByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Host host = notNull(db.getHostByName(site.getId(), name));
        require(permission("read.config", host));
        return host.getConfiguration();
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public HostCfg getHostConfig(BergamotDB db, @IsaObjectId() UUID id)
    {
        Host host = notNull(db.getHost(id));
        require(permission("read.config", host));
        return host.getConfiguration();
    }
}
