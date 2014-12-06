package com.intrbiz.bergamot.ui.router.admin;

import static com.intrbiz.Util.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;

import javax.xml.bind.JAXBException;

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
import com.intrbiz.bergamot.model.ConfigChange;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/admin/configchange")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
public class ConfigChangeAdminRouter extends Router<BergamotApp>
{
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @SessionVar("site") Site site)
    {
        var("changes", db.listConfigChanges(site.getId()));
        encode("admin/configchange/index");
    }
    
    @Any("/create")
    @WithDataAdapter(BergamotDB.class)
    public void create(
            BergamotDB db, 
            @SessionVar("site") Site site,
            @Param("summary") String summary,
            @Param("description") String description
    ) throws IOException
    {
        ConfigChange change = new ConfigChange(site.getId(), new BergamotCfg(site.getName(), summary, description));
        db.setConfigChange(change);
        redirect(path("/admin/configchange/edit/id/" + change.getId()));
    }
    
    @Get("/view/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void view(BergamotDB db, @AsUUID UUID id)
    {
        var("change", db.getConfigChange(id));
        encode("admin/configchange/view");
    }
    
    @Get("/edit/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void edit(BergamotDB db, @AsUUID UUID id)
    {
        ConfigChange change = var("change", db.getConfigChange(id));
        sessionVar("current_change", change.getId());
        encode("admin/configchange/edit");
    }
    
    @Post("/edit/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void save(BergamotDB db, @AsUUID UUID id, @Param("change_configuration") String configuration, @SessionVar("site") Site site) throws IOException, JAXBException
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
    public void add(BergamotDB db, @SessionVar("site") Site site, String type, @AsUUID UUID id) throws IOException
    {
        TemplatedObjectCfg<?> cfg = (TemplatedObjectCfg<?>) db.getConfig(id).getConfiguration();
        // update the change
        UUID currentChangeId = sessionVar("current_change");
        ConfigChange change = currentChangeId == null ? new ConfigChange(site.getId(), new BergamotCfg(site.getName(), "Edit " + type, null)) : db.getConfigChange(currentChangeId);
        // remove the object id
        ((NamedObjectCfg<?>) cfg).setId(null);
        // add the object
        ((BergamotCfg) change.getConfiguration()).addObject(cfg);
        // update
        db.setConfigChange(change);
        // edit
        redirect(path("/admin/configchange/edit/id/" + change.getId()));
    }
    
    @Any("/remove/:type/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void remove(BergamotDB db, @SessionVar("site") Site site, String type, @AsUUID UUID id) throws Exception
    {
        TemplatedObjectCfg<?> cfg = (TemplatedObjectCfg<?>) db.getConfig(id).getConfiguration();
        // update the change
        UUID currentChangeId = sessionVar("current_change");
        ConfigChange change = currentChangeId == null ? new ConfigChange(site.getId(), new BergamotCfg(site.getName(), "Edit " + type, null)) : db.getConfigChange(currentChangeId);
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
    public void saveError(BergamotDB db, @AsUUID UUID id, @Param("change_configuration") String configuration)
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
    public void validate(BergamotDB db, @SessionVar("site") Site site, @AsUUID UUID id)
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
    @WithDataAdapter(BergamotDB.class)
    public void apply(BergamotDB db, @SessionVar("site") Site site, @AsUUID UUID id)
    {
        // nullify any current change
        sessionVar("current_change", null);
        // get the change
        ConfigChange change = var("change", db.getConfigChange(id));
        //
        String taskId = deferredActionWithId(id.toString(), "apply-config-change", site.getId(), id);
        //
        var("change", change);
        var("taskid", taskId);
        //
        encode("admin/configchange/apply");
    }
    
    @Any("/poll/apply/id/:id")
    public void pollApply(@SessionVar("site") Site site, @AsUUID UUID id) throws IOException
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
}
