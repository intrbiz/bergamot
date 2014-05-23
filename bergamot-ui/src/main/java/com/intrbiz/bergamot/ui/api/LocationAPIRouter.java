package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.message.HostMO;
import com.intrbiz.bergamot.model.message.LocationMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;

@Prefix("/api/location")
public class LocationAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    public List<LocationMO> getLocations()
    {
        return this.app().getBergamot().getObjectStore().getLocations().stream().map(Location::toStubMO).collect(Collectors.toList());
    }
    
    @Get("/roots")
    @JSON
    public List<LocationMO> getRootLocations()
    {
        return this.app().getBergamot().getObjectStore().getLocations().stream().filter((e)->{return e.getLocation() == null;}).map(Location::toMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    public LocationMO getLocation(String name)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupLocation(name), Location::toMO);
    }
    
    @Get("/name/:name/children")
    @JSON(notFoundIfNull = true)
    public List<LocationMO> getLocationChildren(String name)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupLocation(name), (e)->{return e.getLocations().stream().map(Location::toMO).collect(Collectors.toList());});
    }
    
    @Get("/name/:name/hosts")
    @JSON(notFoundIfNull = true)
    public List<HostMO> getLocationHosts(String name)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupLocation(name), (e)->{return e.getHosts().stream().map(Host::toMO).collect(Collectors.toList());});
    }
    
    //
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    public LocationMO getLocation(@AsUUID() UUID id)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupLocation(id), Location::toMO);
    }
    
    @Get("/id/:id/children")
    @JSON(notFoundIfNull = true)
    public List<LocationMO> getLocationChildren(@AsUUID() UUID id)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupLocation(id), (e)->{return e.getLocations().stream().map(Location::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/hosts")
    @JSON(notFoundIfNull = true)
    public List<HostMO> getLocationHosts(@AsUUID() UUID id)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupLocation(id), (e)->{return e.getHosts().stream().map(Host::toMO).collect(Collectors.toList());});
    }
}
