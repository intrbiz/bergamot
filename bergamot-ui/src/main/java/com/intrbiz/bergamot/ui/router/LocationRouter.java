package com.intrbiz.bergamot.ui.router;

import static com.intrbiz.bergamot.ui.util.Sorter.*;

import java.io.IOException;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Template;

@Prefix("/")
@Template("layout/main")
public class LocationRouter extends Router<BergamotApp>
{    
    @Any("/location")
    public void rediectLocations() throws IOException
    {
        redirect("/location/");
    }
    
    @Any("/location/")
    public void showLocations()
    {
        Bergamot bergamot = this.app().getBergamot();
        model("locations", orderLocationsByStatus(bergamot.getObjectStore().getRootLocations()));
        encode("location/index");
    }
    
    @Any("/location/name/:name")
    public void showHostGroupByName(String name)
    {
        Bergamot bergamot = this.app().getBergamot();
        Location location = model("location", bergamot.getObjectStore().lookupLocation(name));
        model("hosts", orderHostsByStatus(location.getHosts()));
        model("locations", orderLocationsByStatus(location.getLocations()));
        encode("location/hosts");
    }
}
