package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.model.message.HostMO;
import com.intrbiz.bergamot.model.message.ServiceMO;
import com.intrbiz.bergamot.model.message.TrapMO;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;


@Prefix("/api/host")
public class HostAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    public List<HostMO> getHosts()
    {
        return this.app().getBergamot().getObjectStore().getHosts().stream().map(Host::toStubMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    public HostMO getHost(String name)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupHost(name), Host::toMO);
    }
    
    @Get("/name/:name/state")
    @JSON(notFoundIfNull = true)
    public CheckStateMO getHostState(String name)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupHost(name), (h)->{return h.getState().toMO();});
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    public HostMO getHost(@AsUUID UUID id)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupHost(id), Host::toMO);
    }
    
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    public CheckStateMO getHostState(@AsUUID UUID id)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupHost(id), (h)->{return h.getState().toMO();});
    }
    
    @Get("/name/:name/services")
    @JSON(notFoundIfNull = true)
    public List<ServiceMO> getHostServices(String name)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupHost(name), (e)->{return e.getServices().stream().map(Service::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/services")
    @JSON(notFoundIfNull = true)
    public List<ServiceMO> getHostServices(@AsUUID UUID id)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupHost(id), (e)->{return e.getServices().stream().map(Service::toMO).collect(Collectors.toList());});
    }
    
    @Get("/name/:name/traps")
    @JSON(notFoundIfNull = true)
    public List<TrapMO> getHostTraps(String name)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupHost(name), (e)->{return e.getTraps().stream().map(Trap::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/traps")
    @JSON(notFoundIfNull = true)
    public List<TrapMO> getHostTraps(@AsUUID UUID id)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupHost(id), (e)->{return e.getTraps().stream().map(Trap::toMO).collect(Collectors.toList());});
    }
}
