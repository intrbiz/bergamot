package com.intrbiz.bergamot.ui.api;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.message.ServiceMO;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;


@Prefix("/api/service")
public class ServiceAPIRouter extends Router<BergamotApp>
{    
    @Get("/name/:host/:name")
    @JSON(notFoundIfNull = true)
    public ServiceMO getService(String hostName, String name)
    {    
        return null; //return Util.nullable((Service)Util.nullable(this.app().getBergamot().getObjectStore().lookupHost(hostName), (h)->{return h.getService(name);}), Service::toMO);
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    public ServiceMO getService(@AsUUID UUID id)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupService(id), Service::toMO);
    }
    
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    public CheckStateMO getServiceState(@AsUUID UUID id)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupService(id), (s)->{return s.getState().toMO();});
    }
    
    @Get("/name/:host/:name/state")
    @JSON(notFoundIfNull = true)
    public CheckStateMO getServiceState(String hostName, String name)
    {    
        return null; //return Util.nullable((Service)Util.nullable(this.app().getBergamot().getObjectStore().lookupHost(hostName), (h)->{return h.getService(name);}), (s)->{return s.getState().toMO();});
    }
}
