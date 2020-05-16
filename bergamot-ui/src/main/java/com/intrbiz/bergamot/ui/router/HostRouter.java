package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.GroupCfg;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.config.model.LocationCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.importer.BergamotImportReport;
import com.intrbiz.bergamot.metadata.GetBergamotSite;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.ConfigChange;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.CurrentPrincipal;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.ListParam;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/host")
@Template("layout/main")
@RequireValidPrincipal()
public class HostRouter extends Router<BergamotUI>
{   
    private Logger logger = Logger.getLogger(HostRouter.class);
    
    @Any("/name/:name")
    @WithDataAdapter(BergamotDB.class)
    public void host(BergamotDB db, String name, @GetBergamotSite() Site site)
    {
        Host host = model("host", notNull(db.getHostByName(site.getId(), name)));
        require(permission("read", host));
        model("alerts", db.getAllAlertsForCheck(host.getId(), 3, 0));
        encode("host/detail");
    }
    
    @Any("/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void host(BergamotDB db, @IsaObjectId UUID id)
    {
        Host host = model("host", notNull(db.getHost(id)));
        require(permission("read", host));
        model("alerts", db.getAllAlertsForCheck(id, 3, 0));
        encode("host/detail");
    }
    
    @Any("/execute/:id")
    @WithDataAdapter(BergamotDB.class)
    public void executeHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Host host = db.getHost(id);
        if (host != null)
        {
            if (permission("execute", host)) action("execute-check", host);
        }
        redirect("/host/id/" + id);
    }
    
    @Any("/enable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void enableHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Host host = notNull(db.getHost(id));
        require(permission("enable", host));
        // enable the host
        host.setEnabled(true);
        db.setHost(host);
        // update scheduling
        action("enable-check", host);
        redirect("/host/id/" + id);
    }
    
    @Any("/disable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void disableHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Host host = notNull(db.getHost(id));
        require(permission("disable", host));
        // disable the host
        host.setEnabled(false);
        db.setHost(host);
        // update scheduler
        action("disable-check", host);
        redirect("/host/id/" + id);
    }
    
    @Any("/suppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void suppressHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Host host = notNull(db.getHost(id));
        require(permission("suppress", host));
        // suppress the host
        action("suppress-check", host);
        redirect("/host/id/" + id);
    }
    
    @Any("/unsuppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void unsuppressHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        Host host = notNull(db.getHost(id));
        require(permission("unsuppress", host));
        // unsuppress the host
        action("unsuppress-check", host);
        redirect("/host/id/" + id);
    }
    
    @Any("/execute-services/:id")
    @WithDataAdapter(BergamotDB.class)
    public void executeServicesOnHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        for (Service service : db.getServicesOnHost(id))
        {
            if (permission("execute", service)) action("execute-check", service);
        }
        redirect("/host/id/" + id);
    }
    
    @Any("/suppress-all/:id")
    @WithDataAdapter(BergamotDB.class)
    public void suppressServicesOnHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        for (Service service : db.getServicesOnHost(id))
        {
            if (permission("suppress", service)) action("suppress-check", service);
        }
        for (Trap trap : db.getTrapsOnHost(id))
        {
            if (permission("suppress", trap)) action("suppress-check", trap);
        }
        redirect("/host/id/" + id);
    }
    
    @Any("/unsuppress-all/:id")
    @WithDataAdapter(BergamotDB.class)
    public void unsuppressServicesOnHost(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        for (Service service : db.getServicesOnHost(id))
        {
            if (permission("unsuppress", service)) action("unsuppress-check", service);
        }
        for (Trap trap : db.getTrapsOnHost(id))
        {
            if (permission("unsuppress", trap)) action("suppress-check", trap);
        }
        redirect("/host/id/" + id);
    }
    
    @Get("/create")
    @WithDataAdapter(BergamotDB.class)
    public void create(BergamotDB db, @GetBergamotSite() Site site)
    {
        var("templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(HostCfg.class)).stream().filter((t) -> permission("read", t.getId())).sorted((a, b) -> a.getSummary().compareTo(b.getSummary())).collect(Collectors.toList()));
        var("locations", db.listLocations(site.getId()).stream().filter((l) -> permission("read", l)).sorted((a, b) -> a.getSummary().compareTo(b.getSummary())).collect(Collectors.toList()));
        var("groups", db.listGroups(site.getId()).stream().filter((g) -> permission("read", g)).sorted((a, b) -> a.getSummary().compareTo(b.getSummary())).collect(Collectors.toList()));
        encode("/host/create");
    }
    
    @Post("/create")
    @WithDataAdapter(BergamotDB.class)
    public void doCreate(
            BergamotDB db, 
            @GetBergamotSite() Site site,
            @CurrentPrincipal() Contact user,
            @Param("host_extends") @IsaObjectId(mandatory = true) UUID templateId,
            @Param("host_summary") @CheckStringLength(mandatory = true, max = 255) String summary,
            @Param("host_name") @CheckStringLength(mandatory = true, max = 255) String name,
            @Param("host_address") @CheckStringLength(mandatory = true, max = 255) String address,
            @Param("host_external_ref") @CheckStringLength(mandatory = true, max = 255) String externalRef,
            @Param("host_agent_id") @IsaObjectId(mandatory = false) UUID agentId,
            @Param("host_agent_id_generate") String generateAgentConfig,
            @Param("host_location") @IsaObjectId(mandatory = false) UUID locationId,
            @Param("host_description") @CheckStringLength(mandatory = false, max = 4096) String description,
            @ListParam("host_group") @IsaObjectId(mandatory = false) List<UUID> groupGroups
    )
    {
        // create the configuration object we are going to add
        HostCfg config = new HostCfg();
        config.setSummary(summary);
        config.setName(name);
        if (! Util.isEmpty(address)) config.setAddress(address);
        if (! Util.isEmpty(externalRef)) config.setExternalRef(externalRef);
        if (! Util.isEmpty(description)) config.setDescription(description);
        // extends
        if (templateId == null) throw new RuntimeException("No template given");
        Config extendsCfg = db.getConfig(templateId);
        if (extendsCfg != null && extendsCfg.getConfiguration() instanceof HostCfg)
        {
            config.getInheritedTemplates().add(extendsCfg.getConfiguration().getName());
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
        // groups
        if (groupGroups != null && (! groupGroups.isEmpty()))
        {
            for (UUID groupId : groupGroups)
            {
                if (groupId != null)
                {
                    Config groupCfg = db.getConfig(groupId);
                    if (groupCfg != null && groupCfg.getConfiguration() instanceof GroupCfg)
                    {
                        config.getGroups().add(groupCfg.getConfiguration().getName());
                    }
                }
            }
        }
        // agent id
        if ("yes".equalsIgnoreCase(generateAgentConfig))
        {
            agentId = site.randomObjectId();
        }
        if (agentId != null) config.setAgentId(agentId);
        // the container
        BergamotCfg configContainer = new BergamotCfg();
        configContainer.setSite(site.getName());
        configContainer.setSummary("Create host: " + name);
        configContainer.getHosts().add(config);
        logger.info("Creating host " + name + ":\n" + configContainer);
        // create the configuration change
        ConfigChange change = new ConfigChange(site.getId(), user, configContainer);
        db.setConfigChange(change);
        // apply the change
        BergamotImportReport report = action("apply-config-change", site.getId(), change.getId(), balsa().url(balsa().path("/reset")), user);
        logger.info("Created host " + name + " success=" + report.isSuccessful() + ":\n" + report.toString());
        // success
        var("report", report);
        var("config", config);
        var("change", change);
        encode("host/created");
    }
}
