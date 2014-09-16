package com.intrbiz.bergamot.ui.router.admin;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.TeamCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.bergamot.ui.permissions.UIPermissions;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/admin/team")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
@RequirePermission("ui.admin.team")
public class TeamAdminRouter extends Router<BergamotApp>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @SessionVar("site") Site site)
    {
        model("teams", db.listTeams(site.getId()));
        model("team_templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(TeamCfg.class)));
        encode("admin/team/index");
    }
    
    @Get("/configure/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void showConfigure(BergamotDB db, @AsUUID UUID timeperiodId, @SessionVar("site") Site site)
    {
        model("team", Util.nullable(db.getConfig(timeperiodId), Config::getResolvedConfiguration));
        model("templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(TeamCfg.class)));
        model("teams", db.listConfig(site.getId(), Configuration.getRootElement(TeamCfg.class)));
        model("permissions", UIPermissions.load().getPermissions());
        encode("admin/team/configure");
    }
    
    @Get("/configure")
    @WithDataAdapter(BergamotDB.class)
    public void showConfigureNew(BergamotDB db, @SessionVar("site") Site site)
    {
        model("team", new TeamCfg());
        model("templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(TeamCfg.class)));
        model("teams", db.listConfig(site.getId(), Configuration.getRootElement(TeamCfg.class)));
        model("permissions", UIPermissions.load().getPermissions());
        encode("admin/team/configure");
    }
}
