package com.intrbiz.bergamot.ui.api;

import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.ServiceCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.ServiceMO;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;
import com.intrbiz.metadata.doc.Desc;
import com.intrbiz.metadata.doc.Title;

@Title("Service API Methods")
@Desc({
    "Services represent things which run on Hosts.  These API calls provide information on configured Service checks and their current state."
})
@Prefix("/api/service")
@RequireValidPrincipal()
public class ServiceAPIRouter extends Router<BergamotUI>
{    
    @Title("Get service")
    @Desc({
        "Get the service check on the given host by name."
    })
    @Get("/name/:host/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ServiceMO getServiceByName(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {   
        Service service = notNull(db.getServiceOnHostByName(site.getId(), hostName, name));
        require(permission("read", service));
        return service.toMO(currentPrincipal());
    }
    
    @Title("Get service")
    @Desc({
        "Get the service identified by the given UUID."
    })
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ServiceMO getService(BergamotDB db, @IsaObjectId() UUID id)
    {
        Service service = notNull(db.getService(id));
        require(permission("read", service));
        return service.toMO(currentPrincipal());
    }
    
    @Title("Get service state")
    @Desc({
        "Get the state of the service identified by the given UUID."
    })
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getServiceState(BergamotDB db, @IsaObjectId() UUID id)
    {
        Service service = notNull(db.getService(id));
        require(permission("read", service));
        return service.getState().toMO(currentPrincipal());
    }
    
    @Title("Get service state")
    @Desc({
        "Get the state of the service on the given host by name."
    })
    @Get("/name/:host/:name/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getServiceStateByName(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {    
        Service service = notNull(db.getServiceOnHostByName(site.getId(), hostName, name));
        require(permission("read", service));
        return service.getState().toMO(currentPrincipal());
    }
    
    @Title("Execute a service check")
    @Desc({
        "Execute a check immediately for the service identified by the given UUID."
    })
    @Get("/id/:id/execute")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String executeService(BergamotDB db, @IsaObjectId() UUID id)
    { 
        Service service = notNull(db.getService(id));
        require(permission("execute", service));
        action("execute-check", service);
        return "Ok";
    }
    
    @Title("Suppress a service")
    @Desc({
        "Suppress the service identified by the given UUID, this will prevent any alerts being raised for this service."
    })
    @Get("/id/:id/suppress")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String suppressService(BergamotDB db, @IsaObjectId() UUID id)
    { 
        Service service = notNull(db.getService(id));
        require(permission("suppress", service));
        action("suppress-check", service);
        return "Ok";
    }
    
    @Title("Unsuppress a service")
    @Desc({
        "Unsuppress the service identified by the given UUID, this will stop preventing any alerts being raised for this service."
    })
    @Get("/id/:id/unsuppress")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressService(BergamotDB db, @IsaObjectId() UUID id)
    { 
        Service service = notNull(db.getService(id));
        require(permission("unsuppress", service));
        action("unsuppress-check", service);
        return "Ok";
    }
    
    @Title("Get service configuration")
    @Desc({
        "Get the configuration for the service on the given host by name, returning the XML configuration snippet."
    })
    @Get("/name/:host/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public ServiceCfg getServiceConfigByName(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {
        Service service = notNull(db.getServiceOnHostByName(site.getId(), hostName, name));
        require(permission("read.config", service));
        return service.getConfiguration();
    }
    
    @Title("Get service configuration")
    @Desc({
        "Get the configuration for the service identified by the given UUID, returning the XML configuration snippet."
    })
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public ServiceCfg getServiceConfig(BergamotDB db, @IsaObjectId() UUID id)
    {
        Service service = notNull(db.getService(id));
        require(permission("read.config", service));
        return service.getConfiguration();
    }
}
