package com.intrbiz.bergamot.ui.router.admin;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Change;
import com.intrbiz.bergamot.model.ChangeSet;
import com.intrbiz.bergamot.model.ChangeSetApplier;
import com.intrbiz.bergamot.model.ChangeSetValidationReport;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.ListParam;
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
        List<ChangeSet> sets = new LinkedList<ChangeSet>();
        ChangeSet current = sessionVar("current_changeset");
        if (current != null) sets.add(current);
        var("changesets", sets);
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
    public void update(
            BergamotDB db,
            @Param("summary") @CheckStringLength(min = 1, max = 100, mandatory = true) String summary,
            @Param("description") @CheckStringLength(min = 0, max = 1000) String description,
            @ListParam("change_summary") List<String> changeSummarys,
            @ListParam("change_configuration") List<String> changeConfigs
    )
    {
        // update
        ChangeSet current = sessionVar("current_changeset");
        current.setSummary(summary);
        current.setDescription(description);
        current.getChanges().clear();
        for (int i = 0; i < changeSummarys.size() && i < changeConfigs.size(); i++)
        {
            current.getChanges().add(new Change(changeSummarys.get(i), changeConfigs.get(i)));
        }
        sessionVar("current_changeset", current);
        // edit
        encode("admin/changeset/edit");
    }
    
    @Any("/create")
    @WithDataAdapter(BergamotDB.class)
    public void create(
            BergamotDB db,
            @Param("summary") @CheckStringLength(min = 1, max = 100, mandatory = true) String summary,
            @Param("description") @CheckStringLength(min = 0, max = 1000) String description
    ) throws IOException
    {
        // create the change set
        ChangeSet current = new ChangeSet(summary, description);
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
        current.getChanges().add(new Change("Edit " + type, db.getConfig(id).getConfiguration().toString()));
        sessionVar("current_changeset", current);
        // encode
        redirect(path("/admin/changeset/current"));
    }
    
    @Get("/apply/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void update(BergamotDB db, @SessionVar("site") Site site, @AsUUID UUID id)
    {
        // get the changeset
        ChangeSet current = sessionVar("current_changeset");
        if (current != null)
        {
            ChangeSetApplier applier = new ChangeSetApplier(current, site);
            ChangeSetValidationReport report = applier.validate();
            var("report", report);
            var("changeset", current);
        }
        encode("admin/changeset/apply");
    }
    
    private ChangeSet getOrCreateChangeSet(String summary)
    {
        ChangeSet current = sessionVar("current_changeset");
        if (current == null)
        {
            current = new ChangeSet(summary, "");
            sessionVar("current_changeset", current);
        }
        return current;
    }
}
