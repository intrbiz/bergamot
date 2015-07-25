package com.intrbiz.bergamot.ui.api;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.ServiceCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.ServiceMO;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;


@Prefix("/api/service")
@RequireValidPrincipal()
public class ServiceAPIRouter extends Router<BergamotApp>
{    
    @Get("/name/:host/:name")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.service")
    @WithDataAdapter(BergamotDB.class)
    public ServiceMO getService(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {    
        return Util.nullable(db.getServiceOnHostByName(site.getId(), hostName, name), Service::toMO);
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.service")
    @WithDataAdapter(BergamotDB.class)
    public ServiceMO getService(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getService(id), Service::toMO);
    }
    
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.service")
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getServiceState(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getService(id), (s)->{return s.getState().toMO();});
    }
    
    @Get("/name/:host/:name/state")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.service")
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getServiceState(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {    
        return Util.nullable(db.getServiceOnHostByName(site.getId(), hostName, name), (s)->{return s.getState().toMO();});
    }
    
    @Get("/id/:id/execute")
    @JSON()
    @RequirePermission("api.write.service.execute")
    @WithDataAdapter(BergamotDB.class)
    public String executeService(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Service service = db.getService(id);
        if (service == null) throw new BalsaNotFound("No service with id '" + id + "' exists.");
        action("execute-check", service);
        return "Ok";
    }
    
    @Get("/id/:id/suppress")
    @JSON()
    @RequirePermission("api.write.service.suppress")
    @WithDataAdapter(BergamotDB.class)
    public String suppressService(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Service service = db.getService(id);
        if (service == null) throw new BalsaNotFound("No service with id '" + id + "' exists.");
        action("suppress-check", service);
        return "Ok";
    }
    
    @Get("/id/:id/unsuppress")
    @JSON()
    @RequirePermission("api.write.service.unsuppress")
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressService(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Service service = db.getService(id);
        if (service == null) throw new BalsaNotFound("No service with id '" + id + "' exists.");
        action("unsuppress-check", service);
        return "Ok";
    }
    
    @Get("/name/:host/:name/config.xml")
    @XML(notFoundIfNull = true)
    @RequirePermission("api.read.service.config")
    @WithDataAdapter(BergamotDB.class)
    public ServiceCfg getServiceConfig(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {
        return Util.nullable(db.getServiceOnHostByName(site.getId(), hostName, name), Service::getConfiguration);
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @RequirePermission("api.read.service.config")
    @WithDataAdapter(BergamotDB.class)
    public ServiceCfg getServiceConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getService(id), Service::getConfiguration);
    }
}
