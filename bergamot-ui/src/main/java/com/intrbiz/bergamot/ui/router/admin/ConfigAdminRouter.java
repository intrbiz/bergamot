package com.intrbiz.bergamot.ui.router.admin;

import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.GetBergamotSite;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/admin/config")
@Template("layout/main")
@RequireValidPrincipal()
public class ConfigAdminRouter extends Router<BergamotApp>
{        
    @Get("/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void showConfigure(BergamotDB db, @IsaObjectId UUID id, @GetBergamotSite() Site site)
    {
        require(permission("read.config", id));
        var("config", db.getConfig(id));
        encode("admin/config/view");
    }
}
