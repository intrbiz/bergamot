package com.intrbiz.bergamot.ui.router.admin;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.GetBergamotSite;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/admin/securitydomain")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
public class SecurityDomainAdminRouter extends Router<BergamotUI>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @GetBergamotSite() Site site)
    {
        model("securitydomains", db.listSecurityDomains(site.getId()));
        encode("admin/securitydomain/index");
    }
}
