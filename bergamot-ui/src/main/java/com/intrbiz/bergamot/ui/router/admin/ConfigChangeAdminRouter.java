package com.intrbiz.bergamot.ui.router.admin;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.ConfigChange;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Param;
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
    
    @Any("/edit/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void edit(BergamotDB db, @AsUUID UUID id)
    {
        ConfigChange change = var("change", db.getConfigChange(id));
        sessionVar("current_change", change.getId());
        encode("admin/configchange/edit");
    }
    
    @Any("/save/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void save(BergamotDB db, @AsUUID UUID id, @Param("change_configuration") String configuration) throws IOException
    {
        BergamotCfg cfg = BergamotCfg.fromString(BergamotCfg.class, configuration);
        // update
        ConfigChange change = db.getConfigChange(id);
        change.setConfiguration(cfg);
        db.setConfigChange(change);
        // back to list
        redirect(path("/admin/configchange/"));
    }
    
    @Any("/add/:type/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void edit(BergamotDB db, @SessionVar("site") Site site, String type, @AsUUID UUID id) throws IOException
    {
        TemplatedObjectCfg<?> cfg = (TemplatedObjectCfg<?>) db.getConfig(id).getConfiguration();
        // update the change
        UUID currentChangeId = sessionVar("current_change");
        ConfigChange change = currentChangeId == null ? new ConfigChange(site.getId(), new BergamotCfg(site.getName(), "Edit " + type, null)) : db.getConfigChange(currentChangeId);
        // add the object
        ((BergamotCfg) change.getConfiguration()).addObject(cfg);
        // update
        db.setConfigChange(change);
        // edit
        redirect(path("/admin/configchange/edit/id/" + change.getId()));
    }
}
