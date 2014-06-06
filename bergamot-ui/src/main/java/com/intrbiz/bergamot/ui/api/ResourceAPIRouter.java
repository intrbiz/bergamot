package com.intrbiz.bergamot.ui.api;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.model.message.ResourceMO;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;


@Prefix("/api/resource")
public class ResourceAPIRouter extends Router<BergamotApp>
{    
    @Get("/name/:cluster/:name")
    @JSON(notFoundIfNull = true)
    public ResourceMO getResource(String clusterName, String name)
    {    
        return null; //return Util.nullable((Resource)Util.nullable(this.app().getBergamot().getObjectStore().lookupCluster(clusterName), (h)->{return h.getResource(name);}), Resource::toMO);
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    public ResourceMO getResource(@AsUUID UUID id)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupResource(id), Resource::toMO);
    }
    
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    public CheckStateMO getResourceState(@AsUUID UUID id)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupResource(id), (r)->{return r.getState().toMO();});
    }
    
    @Get("/name/:host/:name/state")
    @JSON(notFoundIfNull = true)
    public CheckStateMO getResourceState(String clusterName, String name)
    {    
        return null; //return Util.nullable((Resource)Util.nullable(this.app().getBergamot().getObjectStore().lookupCluster(clusterName), (h)->{return h.getResource(name);}), (r)->{return r.getState().toMO();});
    }
}
