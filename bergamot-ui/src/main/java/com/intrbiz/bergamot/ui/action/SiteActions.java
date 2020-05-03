package com.intrbiz.bergamot.ui.action;

import java.io.File;
import java.util.Collection;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.action.BalsaAction;
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
import com.intrbiz.bergamot.model.ProxyKey;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.event.site.DeinitSite;
import com.intrbiz.bergamot.model.message.event.site.InitSite;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.bergamot.ui.router.global.InstallBean;
import com.intrbiz.lamplighter.data.LamplighterDB;
import com.intrbiz.metadata.Action;

public class SiteActions implements BalsaAction<BergamotApp>
{
    public static final Logger logger = Logger.getLogger(SiteActions.class);

    public SiteActions()
    {
        super();
    }

    @Action("site-init")
    public void initSite(Site site)
    {
        this.app().getProcessor().getSiteEventTopic().publish(new InitSite(site.getId(), site.getName()));
    }
    
    @Action("site-deinit")
    public void disableCheck(Site site)
    {
        this.app().getProcessor().getSiteEventTopic().publish(new DeinitSite(site.getId(), site.getName()));
    }
    
    @Action("site-create")
    public UUID createSite(InstallBean install) throws Exception
    {
        // the site name
        UUID   siteId      = Site.randomSiteId();
        String siteName    = install.getSiteName();
        String siteSummary = install.getSiteSummary();
        // TODO
        File templateConfigDir = new File(Util.coalesceEmpty(System.getenv("BERGAMOT_SITE_CONFIG_TEMPLATE"), System.getProperty("bergamot.site.config.template"), "cfg/template"));
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
        admin.getInheritedTemplates().add("generic-contact");
        admin.getTeams().add("bergamot-admins");
        // load the site configuration template and inject our admin user
        Collection<ValidatedBergamotConfiguration> vbcfgs = new BergamotConfigReader()
                .overrideSiteName(siteName)
                .includeDir(new File(templateConfigDir, "base"))
                .includeDir(new File(templateConfigDir, "bergamot"))
                .inject(new BergamotCfg(siteName, admin))
                .build();
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
            BergamotImportReport report = new BergamotConfigImporter(vbcfg, app().getProcessor().getSchedulingPoolCount()).offline().defaultPassword(install.getPassword()).requirePasswordChange(false).resetState(true).importConfiguration();
            logger.info(report.toString());
        }
        // broadcast a site init event
        this.initSite(site);
        // Create bergamot agent and proxy keys
        try (BergamotDB db = BergamotDB.connect())
        {
            db.setAgentKey(AgentKey.create(site.getId(), "Default Bergamot Agent Key"));
            db.setProxyKey(ProxyKey.create(site.getId(), "Default Bergamot Proxy Key"));
        }
        // all done
        logger.info("Created the site '" + siteName + "' and imported the default configuration, have fun :)");
        return siteId;
    }
    
    @Action("site-install")
    public void installSite(InstallBean install) throws Exception
    {
        UUID siteId = this.createSite(install);
        // mark first install as complete
        try (BergamotDB db = BergamotDB.connect())
        {
            // add our newly created admin to the global admin list
            Contact theAdmin = db.getContactByName(siteId, install.getUsername());
            GlobalSetting globalAdmins = new GlobalSetting(GlobalSetting.NAME.GLOBAL_ADMINS);
            globalAdmins.addParameter(theAdmin.getId().toString(), "true");
            db.setGlobalSetting(globalAdmins);
            // create global proxy key
            db.setProxyKey(ProxyKey.createGlobal("Default Bergamot Proxy Key"));
            // mark first install as complete
            db.setGlobalSetting(new GlobalSetting(GlobalSetting.NAME.FIRST_INSTALL, "complete"));
        }
    }
}
