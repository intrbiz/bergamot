package com.intrbiz.bergamot.ui.router.global;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.BergamotConfigReader;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg.ObjectState;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.importer.BergamotConfigImporter;
import com.intrbiz.bergamot.importer.BergamotImportReport;
import com.intrbiz.bergamot.model.AgentKey;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.GlobalSetting;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.lamplighter.data.LamplighterDB;
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
    private static final Logger logger = Logger.getLogger(FirstInstallRouter.class);
    
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
        this.createSite(sessionModel("install"));
        // done!
        encode("global/install/complete");
    }
    
    private void createSite(InstallBean install) throws Exception
    {
        // the site name
        UUID   siteId      = Site.randomSiteId();
        String siteName    = install.getSiteName();
        String siteSummary = install.getSiteSummary();
        // TODO
        File templateConfigDir = new File(System.getProperty("bergamot.site.config.template", "cfg/template"));
        // now check that we can create the site and it's aliases
        try (BergamotDB db = BergamotDB.connect())
        {
            // check the site does not already exist!
            if (db.getSiteByName(siteName) != null)
            {
                throw new BalsaException("A site with the name '" + siteName + "' already exists!");
            }
        }
        // create the admin user configuration
        ContactCfg admin = new ContactCfg();
        admin.setName(install.getUsername());
        admin.setEmail(install.getUserEmail());
        admin.setFirstName(install.getUserFirstName());
        admin.setFamilyName(install.getUserLastName());
        admin.setFullName((Util.coalesce(admin.getFirstName(), "") + " " + Util.coalesce(admin.getFamilyName())).trim());
        admin.setMobile(install.getUserMobile());
        admin.setPager(install.getUserMobile());
        admin.setSummary(admin.getFullName());
        admin.setObjectState(ObjectState.PRESENT);
        admin.getInheritedTemplates().add("generic_contact");
        admin.getTeams().add("bergamot-admins");
        // load the site configuration template and inject our admin user
        Collection<ValidatedBergamotConfiguration> vbcfgs = new BergamotConfigReader().overrideSiteName(siteName).includeDir(templateConfigDir).inject(new BergamotCfg(siteName, admin)).build();
        // assert the configuration is valid
        for (ValidatedBergamotConfiguration vbcfg : vbcfgs)
        {
            if (! vbcfg.getReport().isValid())
            {
                throw new BalsaException(vbcfg.getReport().toString());
            }
            else
            {
                logger.info(vbcfg.getReport().toString());
            }
        }
        // now create the site
        Site site = new Site(siteId, siteName, siteSummary);
        try (BergamotDB db = BergamotDB.connect())
        {
            db.setSite(site);
        }
        // setup the readings
        try (LamplighterDB db = LamplighterDB.connect())
        {
            db.setupSiteReadings(siteId);
        }
        // now import the site config
        for (ValidatedBergamotConfiguration vbcfg : vbcfgs)
        {
            BergamotImportReport report = new BergamotConfigImporter(vbcfg).offline().defaultPassword(install.getPassword()).requirePasswordChange(false).resetState(true).importConfiguration();
            logger.info(report.toString());
        }
        // broadcast a site init event
        action("site-init", site);
        // Create bergamot agent keys
        try (BergamotDB db = BergamotDB.connect())
        {
            db.setAgentKey(AgentKey.create(site.getId(), "Default Bergamot Agent Key"));
        }
        // all done
        logger.info("Created the site '" + siteName + "' and imported the default configuration, have fun :)");
        // mark first install as complete
        try (BergamotDB db = BergamotDB.connect())
        {
            // add our newly created admin to the global admin list
            Contact theAdmin = db.getContactByName(siteId, admin.getName());
            GlobalSetting globalAdmins = new GlobalSetting(GlobalSetting.NAME.GLOBAL_ADMINS);
            globalAdmins.addParameter(theAdmin.getId().toString(), "true");
            db.setGlobalSetting(globalAdmins);
            // mark first install as complete
            db.setGlobalSetting(new GlobalSetting(GlobalSetting.NAME.FIRST_INSTALL, "complete"));
        }
    }
}
