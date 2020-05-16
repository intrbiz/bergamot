package com.intrbiz.bergamot.ui.router.admin;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.GetBergamotSite;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CurrentPrincipal;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/admin")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
public class AdminRouter extends Router<BergamotUI>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @CurrentPrincipal Contact principal, @GetBergamotSite() Site site)
    {
        model("site", site);
        encode("admin/index");
    }
}
