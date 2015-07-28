package com.intrbiz.bergamot.ui.api;

import static com.intrbiz.balsa.BalsaContext.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamReader;

import com.fasterxml.jackson.core.JsonGenerator;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.ResourceCfg;
import com.intrbiz.bergamot.config.model.ServiceCfg;
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.config.model.TrapCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.importer.BergamotImportReport;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.ConfigChange;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.metadata.CheckRegEx;
import com.intrbiz.metadata.Get;
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
    @WithDataAdapter(BergamotDB.class)
    public void applyConfigChange(BergamotDB db, @Var("site") Site site) throws IOException
    {
        try
        {
            // read the change
            XMLStreamReader reader = this.request().getXMLReader();
            JAXBContext ctx = JAXBContext.newInstance(BergamotCfg.class);
            Unmarshaller unm = ctx.createUnmarshaller();
            BergamotCfg cfg = (BergamotCfg) unm.unmarshal(reader);
            // create the config change
            ConfigChange change = new ConfigChange();
            change.setCreated(new Timestamp(System.currentTimeMillis()));
            change.setSummary("Change via API: " + cfg.getSummary());
            change.setDescription(cfg.getDescription());
            change.setSiteId(site.getId());
            change.setId(site.randomObjectId());
            change.setConfiguration(cfg);
            db.setConfigChange(change);
            // apply the change
            BergamotImportReport report = action("apply-config-change", site.getId(), change.getId(), Balsa().url(Balsa().path("/reset")));
            // write out the report
            JsonGenerator json = response().ok().json().getJsonWriter();
            json.writeStartObject();
            json.writeFieldName("stat");
            json.writeString("ok");
            json.writeFieldName("result");
            json.writeString(report.toString());
        }
        catch (Exception e)
        {
            if (! "application/xml".equals(this.request().getContentType())) throw new BalsaException("Request content type must be 'application/xml'");
            // write out the report
            JsonGenerator json = response().status(HTTPStatus.InternalServerError).json().getJsonWriter();
            json.writeStartObject();
            json.writeFieldName("stat");
            json.writeString("ok");
            json.writeFieldName("message");
            json.writeString(e.getMessage());
        }
    }
}
