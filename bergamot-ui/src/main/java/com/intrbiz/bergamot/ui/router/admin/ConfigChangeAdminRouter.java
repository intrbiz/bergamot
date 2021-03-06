package com.intrbiz.bergamot.ui.router.admin;

import static com.intrbiz.Util.*;
import static com.intrbiz.balsa.BalsaContext.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.engine.task.BalsaTaskState;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.NamedObjectCfg;
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg.ObjectState;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.importer.BergamotImportReport;
import com.intrbiz.bergamot.metadata.GetBergamotSite;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.ConfigChange;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.IsaLong;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/admin/configchange")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
public class ConfigChangeAdminRouter extends Router<BergamotUI>
{
    private Logger logger = Logger.getLogger(ConfigChangeAdminRouter.class);
    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @GetBergamotSite() Site site)
    {
        List<ConfigChange> pending = var("pending_changes", db.getPendingConfigChanges(site.getId()));
        List<ConfigChange> applied = var("applied_changes", db.pageConfigChanges(site.getId(), true, 5, 0));
        List<ConfigChange> changes = var("changes", new LinkedList<ConfigChange>());
        changes.addAll(pending);
        changes.addAll(applied);
        encode("admin/configchange/index");
    }
    
    @Any("/history")
    @WithDataAdapter(BergamotDB.class)
    public void history(
            BergamotDB db, 
            @GetBergamotSite() Site site,
            @Param("offset") @IsaLong(min = 0, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = 0L)   long offset,
            @Param("limit")  @IsaLong(min = 1, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = 100L) long limit
    )
    {
        var("changes", db.pageConfigChanges(site.getId(), true, limit, offset));
        var("offset", offset);
        var("limit", limit);
        encode("admin/configchange/history");
    }
    
    @Any("/create")
    @WithDataAdapter(BergamotDB.class)
    public void create(
            BergamotDB db, 
            @GetBergamotSite() Site site,
            @Param("summary") String summary,
            @Param("description") String description
    ) throws IOException
    {
        Contact user = currentPrincipal();
        ConfigChange change = new ConfigChange(site.getId(), user, new BergamotCfg(site.getName(), summary, description));
        db.setConfigChange(change);
        redirect(path("/admin/configchange/edit/id/" + change.getId()));
    }
    
    @Get("/view/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void view(BergamotDB db, @IsaObjectId UUID id)
    {
        var("change", db.getConfigChange(id));
        encode("admin/configchange/view");
    }
    
    @Get("/edit/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void edit(BergamotDB db, @IsaObjectId UUID id)
    {
        ConfigChange change = var("change", db.getConfigChange(id));
        sessionVar("current_change", change.getId());
        encode("admin/configchange/edit");
    }
    
    @Post("/edit/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void save(BergamotDB db, @IsaObjectId UUID id, @Param("change_configuration") String configuration, @GetBergamotSite() Site site) throws IOException, JAXBException
    {
        BergamotCfg cfg = Configuration.read(BergamotCfg.class, new StringReader(configuration));
        // update
        ConfigChange change = db.getConfigChange(id);
        cfg.setSite(site.getName());
        change.setConfiguration(cfg);
        change.setSummary(Util.coalesceEmpty(cfg.getSummary(), change.getSummary(), ""));
        change.setDescription(cfg.getDescription());
        db.setConfigChange(change);
        // nullify the current change id
        sessionVar("current_change", null);
        // back to list
        redirect(path("/admin/configchange/"));
    }
    
    @Any("/add/:type/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void add(BergamotDB db, @GetBergamotSite() Site site, String type, @IsaObjectId UUID id) throws IOException
    {
        TemplatedObjectCfg<?> cfg = (TemplatedObjectCfg<?>) db.getConfig(id).getConfiguration();
        // update the change
        UUID currentChangeId = sessionVar("current_change");
        Contact user = currentPrincipal();
        ConfigChange change = currentChangeId == null ? new ConfigChange(site.getId(), user, new BergamotCfg(site.getName(), "Edit " + type, null)) : db.getConfigChange(currentChangeId);
        // remove the object id
        ((NamedObjectCfg<?>) cfg).setId(null);
        // add the object
        ((BergamotCfg) change.getConfiguration()).addObject(cfg);
        // update
        db.setConfigChange(change);
        // edit
        redirect(path("/admin/configchange/edit/id/" + change.getId()));
    }
    
    @Any("/add/site-parameters")
    @WithDataAdapter(BergamotDB.class)
    public void add(BergamotDB db, @GetBergamotSite() Site site) throws IOException
    {
        // make sure we have a fresh copy of the site
        site = db.getSite(site.getId());
        // update the change
        UUID currentChangeId = sessionVar("current_change");
        Contact user = currentPrincipal();
        ConfigChange change = currentChangeId == null ? new ConfigChange(site.getId(), user, new BergamotCfg(site.getName(), "Edit site parameters", null)) : db.getConfigChange(currentChangeId);
        // copy the site parameters into the config change
        BergamotCfg cfg = ((BergamotCfg) change.getConfiguration());
        for (Entry<String, Parameter> param : site.getParameters().entrySet())
        {
            cfg.addParameter(new CfgParameter(param.getKey(), param.getValue().getDescription(), null, param.getValue().getValue()));
        }
        // update
        db.setConfigChange(change);
        // edit
        redirect(path("/admin/configchange/edit/id/" + change.getId()));
    }
    
    @Any("/remove/:type/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void remove(BergamotDB db, @GetBergamotSite() Site site, String type, @IsaObjectId UUID id) throws Exception
    {
        TemplatedObjectCfg<?> cfg = (TemplatedObjectCfg<?>) db.getConfig(id).getConfiguration();
        // update the change
        UUID currentChangeId = sessionVar("current_change");
        Contact user = currentPrincipal();
        ConfigChange change = currentChangeId == null ? new ConfigChange(site.getId(), user, new BergamotCfg(site.getName(), "Edit " + type, null)) : db.getConfigChange(currentChangeId);
        // remove the object id
        ((NamedObjectCfg<?>) cfg).setId(null);
        // set removed
        cfg.setObjectState(ObjectState.REMOVED);
        // add the object
        ((BergamotCfg) change.getConfiguration()).addObject(cfg);
        // update
        db.setConfigChange(change);
        // edit
        redirect(path("/admin/configchange/edit/id/" + change.getId()));
    }
    
    @Catch(JAXBException.class)
    @Post("/edit/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void saveError(BergamotDB db, @IsaObjectId UUID id, @Param("change_configuration") String configuration)
    {
        JAXBException error = (JAXBException) balsa().getException();
        Throwable linked = error.getLinkedException();
        // the change
        var("change", db.getConfigChange(id));
        var("error", coalesceEmpty(error.getMessage(), nullable(linked, Throwable::getMessage), "Error parsing XML"));
        var("bad_configuration", configuration);
        // edit configuration
        encode("admin/configchange/edit");
    }
    
    @Any("/validate/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void validate(BergamotDB db, @GetBergamotSite() Site site, @IsaObjectId UUID id)
    {
        // nullify any current change
        sessionVar("current_change", null);
        // get the change
        ConfigChange change = var("change", db.getConfigChange(id));
        //
        BergamotCfg cfg = (BergamotCfg) change.getConfiguration();
        ValidatedBergamotConfiguration validated = cfg.validate(db.getObjectLocator(site.getId()));
        //
        var("report", validated.getReport());
        //
        encode("admin/configchange/validate");
    }
    
    @Any("/apply/id/:id")
    @RequirePermission("config.change.apply")
    @WithDataAdapter(BergamotDB.class)
    public void apply(BergamotDB db, @GetBergamotSite() Site site, @IsaObjectId UUID id)
    {
        Contact user = currentPrincipal();
        // nullify any current change
        sessionVar("current_change", null);
        // get the change
        ConfigChange change = var("change", db.getConfigChange(id));
        // compute the reset url we need for registrations
        final String resetUrl = Balsa().url(Balsa().path("/reset"));
        // apply the change
        String taskId = deferredActionWithId(id.toString(), "apply-config-change", site.getId(), id, resetUrl, user);
        //
        var("change", change);
        var("taskid", taskId);
        //
        encode("admin/configchange/apply");
    }
    
    @Any("/poll/apply/id/:id")
    @RequirePermission("config.change.apply")
    public void pollApply(@GetBergamotSite() Site site, @IsaObjectId UUID id) throws IOException
    {
        // get the task state
        BalsaTaskState state = pollDeferredAction(id.toString());
        // output JSON response
        JsonGenerator json = response().ok().json().getJsonWriter();
        json.writeStartObject();
        // output
        if (state != null)
        {
            if (state.isComplete())
            {
                // get the value
                try
                {
                    BergamotImportReport report = state.get();
                    // stat
                    json.writeFieldName("stat");
                    json.writeString("ok");
                    json.writeFieldName("complete");
                    json.writeBoolean(true);
                    json.writeFieldName("result");
                    json.writeString(report.toString());
                }
                catch (Exception e)
                {
                    logger.error("Error polling config change state", e);
                    json.writeFieldName("stat");
                    json.writeString("error");
                    json.writeFieldName("message");
                    json.writeString(e.getMessage());      
                }
            }
            else
            {
                json.writeFieldName("stat");
                json.writeString("ok");
                json.writeFieldName("complete");
                json.writeBoolean(false);
            }
        }
        else
        {
            json.writeFieldName("stat");
            json.writeString("error");
            json.writeFieldName("message");
            json.writeString("No such change");
        }
        json.writeEndObject();
    }
    
    @Any("/remove/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void remove(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        ConfigChange change = db.getConfigChange(id);
        if (change != null && (! change.isApplied()))
        {
            // can't remove an applied change
            db.removeConfigChange(id);
        }
        redirect(path("/admin/configchange/"));
    }
}
