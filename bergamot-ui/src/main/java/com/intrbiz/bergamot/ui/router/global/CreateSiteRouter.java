package com.intrbiz.bergamot.ui.router.global;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
import com.intrbiz.metadata.CurrentPrincipal;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/global/site")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
public class CreateSiteRouter extends Router<BergamotUI>
{
    @Before()
    @Any("**")
    @WithDataAdapter(BergamotDB.class)
    public void requireGlobalAdmin(BergamotDB db, @CurrentPrincipal Contact principal)
    {
        require(var("globalAdmin", principal.isGlobalAdmin()));
    }
    
    @Get("/create")
    public void showCreateSite() throws Exception
    {
        // create our installation form model
        CreateSiteRequest install = createSessionModel("install", CreateSiteRequest.class);
        install.setSiteName("new." + balsa().request().getServerName());
        install.setSiteSummary("Bergamot Monitoring");
        // show the create site
        encode("global/site/create");
    }
    
    @Post("/create")
    @WithDataAdapter(BergamotDB.class)
    public void doCreateSite(BergamotDB db) throws Exception
    {
        // decode the form
        decodeOnly("global/site/create");
        // create the site
        CreateSiteRequest bean = sessionModel("install");
        action("site-create", bean);
        // done!
        redirect(path("/global/admin/"));
    }
}
