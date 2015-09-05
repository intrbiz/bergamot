package com.intrbiz.bergamot.ui.router;

import static com.intrbiz.bergamot.ui.util.Sorter.*;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.LocationCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/location")
@Template("layout/main")
@RequireValidPrincipal()
public class LocationRouter extends Router<BergamotApp>
{
    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void showLocations(BergamotDB db, @SessionVar("site") Site site)
    {
        model("locations", orderLocationsByStatus(permission("read", db.getRootLocations(site.getId()))));
        encode("location/index");
    }
    
    @Any("/name/:name")
    @WithDataAdapter(BergamotDB.class)
    public void showLocationByName(BergamotDB db, String name, @SessionVar("site") Site site)
    {
        Location location = model("location", notNull(db.getLocationByName(site.getId(), name)));
        require(permission("read", location));
        model("hosts", orderHostsByStatus(permission("read", location.getHosts())));
        model("locations", orderLocationsByStatus(permission("read", location.getChildren())));
        encode("location/location");
    }
    
    @Any("/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void showLocationById(BergamotDB db, @IsaObjectId UUID id)
    {
        Location location = model("location", notNull(db.getLocation(id)));
        require(permission("read", location));
        model("hosts", orderHostsByStatus(permission("read", location.getHosts())));
        model("locations", orderLocationsByStatus(permission("read", location.getChildren())));
        encode("location/location");
    }
    
    @Any("/id/:id/execute-all-hosts")
    @WithDataAdapter(BergamotDB.class)
    public void executeHostsInLocation(BergamotDB db, @IsaObjectId UUID id) throws IOException
    { 
        for (Host host : db.getHostsInLocation(id))
        {
            if (permission("execute", host)) action("execute-check", host);
        }
        redirect("/location/id/" + id);
    }
    
    @Get("/create")
    @WithDataAdapter(BergamotDB.class)
    public void create(BergamotDB db, @SessionVar("site") Site site)
    {
        var("templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(LocationCfg.class)).stream().filter((t) -> permission("read", t.getId())).sorted((a, b) -> a.getSummary().compareTo(b.getSummary())).collect(Collectors.toList()));
        var("locations", db.listLocations(site.getId()).stream().filter((l) -> permission("read", l)).sorted((a, b) -> a.getSummary().compareTo(b.getSummary())).collect(Collectors.toList()));
        encode("/location/create");
    }
}
