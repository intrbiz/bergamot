package com.intrbiz.bergamot.ui.router.admin;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.ChangeSet;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/admin/changeset")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
public class ChangeSetAdminRouter extends Router<BergamotApp>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db)
    {
        encode("admin/changeset/index");
    }
    
    @Get("/current")
    @WithDataAdapter(BergamotDB.class)
    public void current(BergamotDB db)
    {
        encode("admin/changeset/edit");
    }
    
    
    @Post("/current")
    @WithDataAdapter(BergamotDB.class)
    public void update(BergamotDB db, @SessionVar("site") Site site, @Param("configuration") String config)
    {
        BergamotCfg cfg = Configuration.fromString(BergamotCfg.class, config);
        cfg.setSite(site.getName());
        // update
        ChangeSet current = sessionVar("current_changeset");
        current.setConfiguration(cfg);
        sessionVar("current_changeset", current);
        // edit
        encode("admin/changeset/edit");
    }
    
    @Any("/create")
    @WithDataAdapter(BergamotDB.class)
    public void create(
            BergamotDB db,
            @SessionVar("site") Site site,
            @Param("summary") @CheckStringLength(min = 1, max = 100, mandatory = true) String summary,
            @Param("description") @CheckStringLength(min = 0, max = 1000) String description
    ) throws IOException
    {
        // create the change set
        ChangeSet current = new ChangeSet(summary, description);
        current.getConfiguration().setSite(site.getName());
        // stash it in the session
        sessionVar("current_changeset", current);
        // edit
        redirect(path("/admin/changeset/current"));
    }
    
    @Any("/add/:type/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void addContact(BergamotDB db, String type, @AsUUID UUID id) throws IOException
    {
        // stash it in the session
        ChangeSet current = getOrCreateChangeSet("Edit " + type);
        current.getConfiguration().addObject((TemplatedObjectCfg<?>) db.getConfig(id).getConfiguration());
        sessionVar("current_changeset", current);
        // encode
        redirect(path("/admin/changeset/current"));
    }
    
    private ChangeSet getOrCreateChangeSet(String summary)
    {
        ChangeSet current = sessionVar("current_changeset");
        if (current == null)
        {
            Site site = sessionVar("site");
            current = new ChangeSet(summary, "");
            current.getConfiguration().setSite(site.getName());
            sessionVar("current_changeset", current);
        }
        return current;
    }
}
