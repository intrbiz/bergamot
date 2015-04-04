package com.intrbiz.bergamot.ui.router;

import static com.intrbiz.bergamot.ui.util.Sorter.*;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/")
@Template("layout/main")
@RequireValidPrincipal()
public class LocationRouter extends Router<BergamotApp>
{    
    @Any("/location")
    public void rediectLocations() throws IOException
    {
        redirect("/location/");
    }
    
    @Any("/location/")
    @WithDataAdapter(BergamotDB.class)
    public void showLocations(BergamotDB db, @SessionVar("site") Site site)
    {
        model("locations", orderLocationsByStatus(db.getRootLocations(site.getId())));
        encode("location/index");
    }
    
    @Any("/location/name/:name")
    @WithDataAdapter(BergamotDB.class)
    public void showLocationByName(BergamotDB db, String name, @SessionVar("site") Site site)
    {
        Location location = model("location", db.getLocationByName(site.getId(), name));
        model("hosts", orderHostsByStatus(location.getHosts()));
        model("locations", orderLocationsByStatus(location.getChildren()));
        encode("location/location");
    }
    
    @Any("/location/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void showLocationById(BergamotDB db, @IsaObjectId UUID id)
    {
        Location location = model("location", db.getLocation(id));
        model("hosts", orderHostsByStatus(location.getHosts()));
        model("locations", orderLocationsByStatus(location.getChildren()));
        encode("location/location");
    }
    
    @Any("/location/id/:id/execute-all-hosts")
    @WithDataAdapter(BergamotDB.class)
    public void executeHostsInLocation(BergamotDB db, @IsaObjectId UUID id) throws IOException
    { 
        for (Host host : db.getHostsInLocation(id))
        {
            action("execute-check", host);
        }
        redirect("/location/id/" + id);
    }
}
