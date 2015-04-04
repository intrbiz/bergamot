package com.intrbiz.bergamot.ui.api;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.ResourceCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.ResourceMO;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;


@Prefix("/api/resource")
@RequireValidPrincipal()
public class ResourceAPIRouter extends Router<BergamotApp>
{    
    @Get("/name/:cluster/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ResourceMO getResource(BergamotDB db, @Var("site") Site site, String clusterName, String name)
    {    
        return Util.nullable(db.getResourceOnClusterByName(site.getId(), clusterName, name), Resource::toMO);
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ResourceMO getResource(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getResource(id), Resource::toMO);
    }
    
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getResourceState(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getResource(id), (r)->{return r.getState().toMO();});
    }
    
    @Get("/name/:host/:name/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getResourceState(BergamotDB db, @Var("site") Site site, String clusterName, String name)
    {    
        return Util.nullable(db.getResourceOnClusterByName(site.getId(), clusterName, name), (r)->{return r.getState().toMO();});
    }
    
    @Get("/name/:host/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ResourceCfg getResourceConfig(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {
        return Util.nullable(db.getResourceOnClusterByName(site.getId(), hostName, name), Resource::getConfiguration);
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public ResourceCfg getResourceConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getResource(id), Resource::getConfiguration);
    }
    
    @Get("/id/:id/suppress")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String suppressResource(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Resource resource = db.getResource(id);
        if (resource == null) throw new BalsaNotFound("No resource with id '" + id + "' exists.");
        action("suppress-check", resource);
        return "Ok";
    }
    
    @Get("/id/:id/unsuppress")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressCluster(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Resource resource = db.getResource(id);
        if (resource == null) throw new BalsaNotFound("No resource with id '" + id + "' exists.");
        action("unsuppress-check", resource);
        return "Ok";
    }
}
