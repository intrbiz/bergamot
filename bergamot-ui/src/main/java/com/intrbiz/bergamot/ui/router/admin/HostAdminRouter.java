package com.intrbiz.bergamot.ui.router.admin;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/admin/host")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
@RequirePermission("ui.admin.host")
public class HostAdminRouter extends Router<BergamotApp>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @SessionVar("site") Site site)
    {
        model("hosts", db.listHosts(site.getId()));
        model("host_templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(HostCfg.class)));
        encode("admin/host/index");
    }
    
    @Get("/configure/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void showConfigure(BergamotDB db, @AsUUID UUID timeperiodId, @SessionVar("site") Site site)
    {
        model("host", Util.nullable(db.getConfig(timeperiodId), Config::getResolvedConfiguration));
        model("templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(HostCfg.class)));
        encode("admin/host/configure");
    }
    
    @Get("/configure")
    @WithDataAdapter(BergamotDB.class)
    public void showConfigureNew(BergamotDB db, @SessionVar("site") Site site)
    {
        model("host", new HostCfg());
        model("templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(HostCfg.class)));
        encode("admin/host/configure");
    }
}
