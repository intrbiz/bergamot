package com.intrbiz.bergamot.ui.router;

import static com.intrbiz.balsa.BalsaContext.*;
import static com.intrbiz.bergamot.ui.util.Sorter.*;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.LocationCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.importer.BergamotImportReport;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.ConfigChange;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.CurrentPrincipal;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/location")
@Template("layout/main")
@RequireValidPrincipal()
public class LocationRouter extends Router<BergamotApp>
{
    private Logger logger = Logger.getLogger(LocationRouter.class);
    
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
    
    @Post("/create")
    @WithDataAdapter(BergamotDB.class)
    public void doCreate(
            BergamotDB db, 
            @SessionVar("site") Site site,
            @CurrentPrincipal() Contact user,
            @Param("location_extends") @IsaObjectId(mandatory = false) UUID templateId,
            @Param("location_summary") @CheckStringLength(mandatory = true, max = 255) String summary,
            @Param("location_name") @CheckStringLength(mandatory = true, max = 255) String name,
            @Param("location_location") @IsaObjectId(mandatory = false) UUID locationId,
            @Param("location_description") @CheckStringLength(mandatory = false, max = 4096) String description,
            @Param("location_workerpool") @CheckStringLength(mandatory = false, max = 255) String workerPool
    )
    {
        // create the configuration object we are going to add
        LocationCfg config = new LocationCfg();
        config.setSummary(summary);
        config.setName(name);
        if (! Util.isEmpty(workerPool)) config.setWorkerPool(workerPool);
        if (! Util.isEmpty(description)) config.setDescription(description);
        // extends
        if (templateId != null)
        {
            Config extendsCfg = db.getConfig(templateId);
            if (extendsCfg != null && extendsCfg.getConfiguration() instanceof LocationCfg)
            {
                config.getInheritedTemplates().add(extendsCfg.getConfiguration().getName());
            }
        }
        // location
        if (locationId != null)
        {
            Config locationCfg = db.getConfig(locationId);
            if (locationCfg != null && locationCfg.getConfiguration() instanceof LocationCfg)
            {
                config.setLocation(locationCfg.getConfiguration().getName());
            }
        }
        // the container
        BergamotCfg configContainer = new BergamotCfg();
        configContainer.setSite(site.getName());
        configContainer.setSummary("Create location: " + name);
        configContainer.getLocations().add(config);
        logger.info("Creating location " + name + ":\n" + configContainer);
        // create the configuration change
        ConfigChange change = new ConfigChange(site.getId(), user, configContainer);
        db.setConfigChange(change);
        // apply the change
        BergamotImportReport report = action("apply-config-change", site.getId(), change.getId(), Balsa().url(Balsa().path("/reset")), user);
        logger.info("Created location " + name + " success=" + report.isSuccessful() + ":\n" + report.toString());
        // success
        var("report", report);
        var("config", config);
        var("change", change);
        encode("location/created");
    }
}
