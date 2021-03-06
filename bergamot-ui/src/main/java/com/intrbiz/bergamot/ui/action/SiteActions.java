package com.intrbiz.bergamot.ui.action;

import java.io.File;
import java.util.Collection;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.action.BalsaAction;
import com.intrbiz.bergamot.BergamotConfig;
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
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.bergamot.ui.router.global.CreateSiteRequest;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.metadata.Action;

public class SiteActions implements BalsaAction<BergamotUI>
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
    public Site createSite(CreateSiteRequest install) throws Exception
    {
        // the site name
        UUID   siteId      = Site.randomSiteId();
        String siteName    = install.getSiteName();
        String siteSummary = install.getSiteSummary();
        // Get the template directory
        File templateConfigDir = BergamotConfig.getBergamotSiteConfigurationTemplatePath();
        // now check that we can create the site and it's aliases
        try (BergamotDB db = BergamotDB.connect())
        {
            // check the site does not already exist!
            if (db.getSiteByName(siteName) != null)
            {
                throw new BalsaException("A site with the name '" + siteName + "' already exists!");
            }
        }
        // generated configuration to inject
        BergamotCfg generatedConfig = new BergamotCfg(siteName);
        generatedConfig.addParameter(new CfgParameter("deployment_mode", "Deployment Mode", null, BergamotConfig.getExpectedProcessors() > 1 ? "cluster" : "standalone"));
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
        generatedConfig.addObject(admin);
        // load the site configuration template and inject our admin user
        Collection<ValidatedBergamotConfiguration> vbcfgs = new BergamotConfigReader()
                .overrideSiteName(siteName)
                .includeDir(new File(templateConfigDir, "base"))
                .includeDir(new File(templateConfigDir, "bergamot"))
                .inject(generatedConfig)
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
        return site;
    }
    
    @Action("site-install")
    public void installSite(CreateSiteRequest install) throws Exception
    {
        Site site = this.createSite(install);
        // mark first install as complete
        try (BergamotDB db = BergamotDB.connect())
        {
            // add our newly created admin to the global admin list
            Contact theAdmin = db.getContactByName(site.getId(), install.getUsername());
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
