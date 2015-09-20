package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.LocationCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.HostMO;
import com.intrbiz.bergamot.model.message.LocationMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListOf;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;

@Prefix("/api/location")
@RequireValidPrincipal()
public class LocationAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @ListOf(LocationMO.class)
    public List<LocationMO> getLocations(BergamotDB db, @Var("site") Site site)
    {
        return db.listLocations(site.getId()).stream().filter((l) -> permission("read", l)).map((x) -> x.toStubMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/roots")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @ListOf(LocationMO.class)
    public List<LocationMO> getRootLocations(BergamotDB db, @Var("site") Site site)
    {
        return db.getRootLocations(site.getId()).stream().filter((l) -> permission("read", l)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public LocationMO getLocationByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Location location = notNull(db.getLocationByName(site.getId(), name));
        require(permission("read", location));
        return location.toMO(currentPrincipal());
    }
    
    @Get("/name/:name/children")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(LocationMO.class)
    public List<LocationMO> getLocationChildrenByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Location location = notNull(db.getLocationByName(site.getId(), name));
        require(permission("read", location));
        return location.getChildren().stream().filter((l) -> permission("read", l)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/name/:name/hosts")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(HostMO.class)
    public List<HostMO> getLocationHostsByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Location location = notNull(db.getLocationByName(site.getId(), name));
        require(permission("read", location));
        return location.getHosts().stream().filter((h) -> permission("read", h)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    //
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public LocationMO getLocation(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Location location = notNull(db.getLocation(id));
        require(permission("read", location));
        return location.toMO(currentPrincipal());
    }
    
    @Get("/id/:id/children")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(LocationMO.class)
    public List<LocationMO> getLocationChildren(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Location location = notNull(db.getLocation(id));
        require(permission("read", location));
        return location.getChildren().stream().filter((l) -> permission("read", l)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/id/:id/hosts")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(HostMO.class)
    public List<HostMO> getLocationHosts(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Location location = notNull(db.getLocation(id));
        require(permission("read", location));
        return location.getHosts().stream().filter((h) -> permission("read", h)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/id/:id/execute-all-hosts")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String executeHostsInLocation(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Location location = notNull(db.getLocation(id));
        int executed = 0;
        for (Host host : location.getHosts())
        {
            if (permission("execute", host))
            {
                action("execute-check", host);
                executed++;
            }
        }
        return "Ok, executed " + executed + " hosts";
    }
    
    @Get("/name/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public LocationCfg getLocationConfigByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Location location = notNull(db.getLocationByName(site.getId(), name));
        require(permission("read.config", location));
        return location.getConfiguration();
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public LocationCfg getLocationConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Location location = notNull(db.getLocation(id));
        require(permission("read.config", location));
        return location.getConfiguration();
    }
}
