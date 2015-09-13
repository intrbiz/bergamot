package com.intrbiz.bergamot.ui.router;

import static com.intrbiz.bergamot.ui.util.Sorter.*;

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
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.importer.BergamotImportReport;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.ConfigChange;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
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
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/group")
@Template("layout/main")
@RequireValidPrincipal()
public class GroupsRouter extends Router<BergamotApp>
{
    private Logger logger = Logger.getLogger(GroupsRouter.class);
    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void showGroups(BergamotDB db, @SessionVar("site") Site site)
    {
        model("groups", orderGroupsByStatus(permission("read", db.getRootGroups(site.getId()))));
        encode("group/index");
    }
    
    @Any("/name/:name")
    @WithDataAdapter(BergamotDB.class)
    public void showHostGroupByName(BergamotDB db, String name, @SessionVar("site") Site site)
    {
        Group group = model("group", notNull(db.getGroupByName(site.getId(), name)));
        require(permission("read", group));
        model("checks", orderCheckByStatus(permission("read", group.getChecks())));
        model("groups", orderGroupsByStatus(permission("read", group.getChildren())));
        encode("group/group");
    }
    
    @Any("/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void showHostGroupByName(BergamotDB db, @IsaObjectId UUID id, @CurrentPrincipal Contact user)
    {
        Group group = model("group", notNull(db.getGroup(id)));
        require(permission("read", group));
        model("checks", orderCheckByStatus(permission("read", group.getChecks())));
        model("groups", orderGroupsByStatus(permission("read", group.getChildren())));
        encode("group/group");
    }
    
    @Any("/execute-all-checks/:id")
    @WithDataAdapter(BergamotDB.class)
    public void executeChecksInGroup(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        for (Check<?,?> check : db.getChecksInGroup(id))
        {
            if (check instanceof ActiveCheck)
            {
                if (permission("execute", check)) action("execute-check", check);
            }
        }
        redirect("/group/id/" + id);
    }
    
    @Get("/create")
    @WithDataAdapter(BergamotDB.class)
    public void create(BergamotDB db, @SessionVar("site") Site site)
    {
        var("templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(GroupCfg.class)).stream().filter((t) -> permission("read", t.getId())).sorted((a, b) -> a.getSummary().compareTo(b.getSummary())).collect(Collectors.toList()));
        var("locations", db.listLocations(site.getId()).stream().filter((l) -> permission("read", l)).sorted((a, b) -> a.getSummary().compareTo(b.getSummary())).collect(Collectors.toList()));
        var("groups", db.listGroups(site.getId()).stream().filter((g) -> permission("read", g)).sorted((a, b) -> a.getSummary().compareTo(b.getSummary())).collect(Collectors.toList()));
        encode("/group/create");
    }
    
    @Post("/create")
    @WithDataAdapter(BergamotDB.class)
    public void doCreate(
            BergamotDB db, 
            @SessionVar("site") Site site,
            @CurrentPrincipal() Contact user,
            @Param("group_extends") @IsaObjectId(mandatory = false) UUID templateId,
            @Param("group_summary") @CheckStringLength(mandatory = true, max = 255) String summary,
            @Param("group_name") @CheckStringLength(mandatory = true, max = 255) String name,
            @Param("group_description") @CheckStringLength(mandatory = false, max = 4096) String description,
            @ListParam("group_group") @IsaObjectId(mandatory = false) List<UUID> groupGroups
    )
    {
        // create the configuration object we are going to add
        GroupCfg config = new GroupCfg();
        config.setSummary(summary);
        config.setName(name);
        if (! Util.isEmpty(description)) config.setDescription(description);
        // extends
        if (templateId != null)
        {
            Config extendsCfg = db.getConfig(templateId);
            if (extendsCfg != null && extendsCfg.getConfiguration() instanceof GroupCfg)
            {
                config.getInheritedTemplates().add(extendsCfg.getConfiguration().getName());
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
        // the container
        BergamotCfg configContainer = new BergamotCfg();
        configContainer.setSite(site.getName());
        configContainer.setSummary("Create group: " + name);
        configContainer.getGroups().add(config);
        logger.info("Creating group " + name + ":\n" + configContainer);
        // create the configuration change
        ConfigChange change = new ConfigChange(site.getId(), user, configContainer);
        db.setConfigChange(change);
        // apply the change
        BergamotImportReport report = action("apply-config-change", site.getId(), change.getId(), balsa().url(balsa().path("/reset")), user);
        logger.info("Created group " + name + " success=" + report.isSuccessful() + ":\n" + report.toString());
        // success
        var("report", report);
        var("config", config);
        var("change", change);
        encode("group/created");
    }
}
