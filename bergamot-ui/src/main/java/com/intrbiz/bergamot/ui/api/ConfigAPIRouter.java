package com.intrbiz.bergamot.ui.api;

import java.util.List;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.ResourceCfg;
import com.intrbiz.bergamot.config.model.ServiceCfg;
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.config.model.TrapCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.metadata.CheckRegEx;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.ListParam;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Prefix;
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
    @WithDataAdapter(BergamotDB.class)
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
    @WithDataAdapter(BergamotDB.class)
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
}
