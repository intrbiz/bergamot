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
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;


@Prefix("/api/service")
@RequireValidPrincipal()
public class ServiceAPIRouter extends Router<BergamotApp>
{    
    @Get("/name/:host/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ServiceMO getServiceByName(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {   
        Service service = notNull(db.getServiceOnHostByName(site.getId(), hostName, name));
        require(permission("read", service));
        return service.toMO(currentPrincipal());
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ServiceMO getService(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Service service = notNull(db.getService(id));
        require(permission("read", service));
        return service.toMO(currentPrincipal());
    }
    
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getServiceState(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Service service = notNull(db.getService(id));
        require(permission("read", service));
        return service.getState().toMO(currentPrincipal());
    }
    
    @Get("/name/:host/:name/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getServiceStateByName(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {    
        Service service = notNull(db.getServiceOnHostByName(site.getId(), hostName, name));
        require(permission("read", service));
        return service.getState().toMO(currentPrincipal());
    }
    
    @Get("/id/:id/execute")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String executeService(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Service service = notNull(db.getService(id));
        require(permission("execute", service));
        action("execute-check", service);
        return "Ok";
    }
    
    @Get("/id/:id/suppress")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String suppressService(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Service service = notNull(db.getService(id));
        require(permission("suppress", service));
        action("suppress-check", service);
        return "Ok";
    }
    
    @Get("/id/:id/unsuppress")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressService(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Service service = notNull(db.getService(id));
        require(permission("unsuppress", service));
        action("unsuppress-check", service);
        return "Ok";
    }
    
    @Get("/name/:host/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public ServiceCfg getServiceConfigByName(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {
        Service service = notNull(db.getServiceOnHostByName(site.getId(), hostName, name));
        require(permission("read.config", service));
        return service.getConfiguration();
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public ServiceCfg getServiceConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Service service = notNull(db.getService(id));
        require(permission("read.config", service));
        return service.getConfiguration();
    }
}
