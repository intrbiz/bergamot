package com.intrbiz.bergamot.ui.router.admin;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.TimePeriodCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.GetBergamotSite;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/admin/timeperiod")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
public class TimePeriodAdminRouter extends Router<BergamotUI>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @GetBergamotSite() Site site)
    {
        model("timeperiods", db.listTimePeriods(site.getId()));
        model("timeperiod_templates", db.listConfigTemplates(site.getId(), Configuration.getRootElement(TimePeriodCfg.class)));
        encode("admin/timeperiod/index");
    }
}
