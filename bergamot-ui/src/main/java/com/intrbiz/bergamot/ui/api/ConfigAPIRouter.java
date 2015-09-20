package com.intrbiz.bergamot.ui.api;

import static com.intrbiz.balsa.BalsaContext.*;

import java.util.LinkedList;
import java.util.List;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.publicresource.PublicResource;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaBadRequest;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.ResourceCfg;
import com.intrbiz.bergamot.config.model.ServiceCfg;
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.config.model.TrapCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.importer.BergamotImportReport;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.ConfigChange;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.api.APIResponse;
import com.intrbiz.bergamot.model.message.api.call.AppliedConfigChange;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CheckRegEx;
import com.intrbiz.metadata.CurrentPrincipal;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListOf;
import com.intrbiz.metadata.ListParam;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;



@Prefix("/api/config")
@RequireValidPrincipal()
public class ConfigAPIRouter extends Router<BergamotApp>
{
    /**
     * Build the site-wide configuration
     */
    @Get("/site.xml")
    @Order(10)
    @XML
    @RequirePermission("config.export")
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public BergamotCfg buildSiteConfig(
            BergamotDB db, 
            @Var("site") Site site, 
            @ListParam("type") @CheckRegEx(value = "time-period|team|contact|command|location|group|host|service|trap|cluster|resource", mandatory = true) List<String> types
    )
    {
        // build the entire site configuration
        BergamotCfg siteCfg = new BergamotCfg();
        // site details
        siteCfg.setSite(site.getName());
        siteCfg.setDescription(site.getDescription());
        siteCfg.setSummary(site.getSummary());
        for (Parameter parameter : site.getParameters())
        {
            siteCfg.addParameter(new CfgParameter(parameter.getName(), null, null, parameter.getValue()));
        }
        // add objects
        if (types.isEmpty())
        {
            // add all the config
            for (Config cfg : db.listConfig(site.getId(), null))
            {
                if ((!(cfg.getConfiguration() instanceof TrapCfg || cfg.getConfiguration() instanceof ServiceCfg || cfg.getConfiguration() instanceof ResourceCfg)) || cfg.isTemplate())
                {
                    siteCfg.addObject((TemplatedObjectCfg<?>) cfg.getConfiguration());
                }
            }
        }
        else
        {
            // certain types
            for (String type : types)
            {
                for (Config cfg : db.listConfig(site.getId(), type))
                {
                    if ((!(cfg.getConfiguration() instanceof TrapCfg || cfg.getConfiguration() instanceof ServiceCfg || cfg.getConfiguration() instanceof ResourceCfg)) || cfg.isTemplate())
                    {
                        siteCfg.addObject((TemplatedObjectCfg<?>) cfg.getConfiguration());
                    }
                }
            }
        }
        return siteCfg;
    }
    
    /**
     * Build the configuration file for a particular type of object
     */
    @Get(value = "/(time-period|team|contact|command|location|group|host|service|trap|cluster|resource)s?.xml", regex = true, as = {"type"})
    @Order(20)
    @XML
    @RequirePermission("config.export")
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public BergamotCfg builObjectConfig(BergamotDB db, @Var("site") Site site, String type)
    {
        // build the entire site configuration
        BergamotCfg siteCfg = new BergamotCfg();
        siteCfg.setSite(site.getName());
        // add all the config
        for (Config cfg : db.listConfig(site.getId(), type))
        {
            if ((!(cfg.getConfiguration() instanceof TrapCfg || cfg.getConfiguration() instanceof ServiceCfg || cfg.getConfiguration() instanceof ResourceCfg)) || cfg.isTemplate())
            {
                siteCfg.addObject((TemplatedObjectCfg<?>) cfg.getConfiguration());
            }
        }
        return siteCfg;
    }
    
    /**
     * Apply a configuration change
     */
    @Post("/apply")
    @RequirePermission("config.change.apply")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public APIResponse applyConfigChange(BergamotDB db, @Var("site") Site site, @XML BergamotCfg config, @CurrentPrincipal() Contact user) throws Exception
    {
        // check that the change has a summary
        require(! Util.isEmpty(config.getSummary()), new BalsaBadRequest("The configuration change must have a summary"));
        // assert the site name
        config.setSite(site.getName());
        // create the config change
        ConfigChange change = new ConfigChange(site.getId(), user, config);
        change.setSummary("Change via API: " + config.getSummary());
        db.setConfigChange(change);
        // apply the change
        BergamotImportReport report = action("apply-config-change", site.getId(), change.getId(), Balsa().url(Balsa().path("/reset")), user);
        // write out the report
        return new AppliedConfigChange(report.toMOUnsafe());
    }
    
    @Any("/exists/:type/:name")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public Boolean objectExists(BergamotDB db, @Var("site") Site site, String type, String name)
    {
        Config config = db.getConfigByName(site.getId(), type, name);
        return config != null;
    }
    
    @Any("/icon/")
    @JSON()
    @ListOf(String.class)
    public List<String> listIcons()
    {
        List<String> ret = new LinkedList<String>();
        // scan the icon folder
        for (PublicResource resource : app().getPublicResourceEngine().get(balsa(), "/images/icons/64/").getChildren())
        {
            if (resource.getName().endsWith(".png")) 
            {
                ret.add(resource.getPath());
            }
        }
        return ret;
    }
}
