package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.HostMO;
import com.intrbiz.bergamot.model.message.LocationMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Var;

@Prefix("/api/location")
public class LocationAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    public List<LocationMO> getLocations(BergamotDB db, @Var("site") Site site)
    {
        return db.listLocations(site.getId()).stream().map(Location::toStubMO).collect(Collectors.toList());
    }
    
    @Get("/roots")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    public List<LocationMO> getRootLocations(BergamotDB db, @Var("site") Site site)
    {
        return db.getRootLocations(site.getId()).stream().filter((e)->{return e.getLocation() == null;}).map(Location::toMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public LocationMO getLocation(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getLocationByName(site.getId(), name), Location::toMO);
    }
    
    @Get("/name/:name/children")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<LocationMO> getLocationChildren(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getLocationByName(site.getId(), name), (e)->{return e.getChildren().stream().map(Location::toMO).collect(Collectors.toList());});
    }
    
    @Get("/name/:name/hosts")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<HostMO> getLocationHosts(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getLocationByName(site.getId(), name), (e)->{return e.getHosts().stream().map(Host::toMO).collect(Collectors.toList());});
    }
    
    //
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public LocationMO getLocation(BergamotDB db, @AsUUID() UUID id)
    {
        return Util.nullable(db.getLocation(id), Location::toMO);
    }
    
    @Get("/id/:id/children")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<LocationMO> getLocationChildren(BergamotDB db, @AsUUID() UUID id)
    {
        return Util.nullable(db.getLocation(id), (e)->{return e.getChildren().stream().map(Location::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/hosts")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<HostMO> getLocationHosts(BergamotDB db, @AsUUID() UUID id)
    {
        return Util.nullable(db.getLocation(id), (e)->{return e.getHosts().stream().map(Host::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/execute-all-hosts")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String executeHostsInLocation(BergamotDB db, @AsUUID UUID id)
    { 
        Location location = db.getLocation(id);
        if (location == null) throw new BalsaNotFound("No location with id '" + id + "' exists.");
        int executed = 0;
        for (Host host : location.getHosts())
        {
            action("execute-check", host);
            executed++;
        }
        return "Ok, executed " + executed + " hosts";
    }
}
