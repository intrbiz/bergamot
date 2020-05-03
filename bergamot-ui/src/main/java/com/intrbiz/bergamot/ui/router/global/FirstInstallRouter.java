package com.intrbiz.bergamot.ui.router.global;

import java.util.List;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.GlobalSetting;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Template;

@Prefix("/global/install")
@Template("layout/install")
public class FirstInstallRouter extends Router<BergamotApp>
{
    @Before()
    @Any("**")
    @WithDataAdapter(BergamotDB.class)
    public void firstInstallFilter(BergamotDB db)
    {
        require(db.getGlobalSetting(GlobalSetting.NAME.FIRST_INSTALL) == null);
    }
    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void showInstallSite(BergamotDB db) throws Exception
    {
        // do we have any existing sites?
        List<Site> sites = db.listSites();
        if (! sites.isEmpty())
        {
            // add all admins as global admins
            GlobalSetting globalAdmins = new GlobalSetting(GlobalSetting.NAME.GLOBAL_ADMINS);
            for (Site site : sites)
            {
                for (Contact contact : db.listContacts(site.getId()))
                {
                    if (contact.hasPermission("ui.admin"))
                        globalAdmins.addParameter(contact.getId().toString(), "true");
                }
            }
            db.setGlobalSetting(globalAdmins);
            // mark first install as complete
            db.setGlobalSetting(new GlobalSetting(GlobalSetting.NAME.FIRST_INSTALL, "complete"));
            // redirect to dashboard
            redirect(path("/"));
        }
        // create our installation form model
        InstallBean install = createSessionModel("install", InstallBean.class);
        install.setSiteName(balsa().request().getServerName());
        install.setSiteSummary("Bergamot Monitoring");
        // show the first install step
        encode("global/install/index");
    }
    
    @Post("/user")
    @WithDataAdapter(BergamotDB.class)
    public void setInstallSite(BergamotDB db)
    {
        // decode the form
        decodeOnly("global/install/index");
        // next step
        encode("global/install/user");
    }
    
    @Get("/user")
    @WithDataAdapter(BergamotDB.class)
    public void showInstallUser(BergamotDB db)
    {
        // next step
        encode("global/install/user");
    }
    
    @Post("/confirm")
    @WithDataAdapter(BergamotDB.class)
    public void setInstallUser(BergamotDB db)
    {
        decodeOnly("global/install/user");
        // next step
        encode("global/install/confirm");
    }
    
    @Post("/go")
    @WithDataAdapter(BergamotDB.class)
    public void goCreateSite(BergamotDB db) throws Exception
    {
        // create the site
        action("site-install", sessionModel("install"));
        // done!
        encode("global/install/complete");
    }
    
    
}
