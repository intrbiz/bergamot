package com.intrbiz.bergamot.ui.router.global;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.GlobalSetting;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;
import com.intrbiz.bergamot.model.message.agent.manager.request.CreateSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.response.CreatedSiteCA;
import com.intrbiz.bergamot.queue.BergamotAgentManagerQueue;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
import com.intrbiz.metadata.CurrentPrincipal;
import com.intrbiz.metadata.IsaUUID;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidAccessTokenForURL;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;
import com.intrbiz.queue.RPCClient;
import com.intrbiz.queue.name.RoutingKey;

@Prefix("/global/admin")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
public class GlobalAdminRouter extends Router<BergamotApp>
{    
    private static final Logger logger = Logger.getLogger(GlobalAdminRouter.class);
    
    @Before()
    @Any("**")
    @WithDataAdapter(BergamotDB.class)
    public void requireGlobalAdmin(BergamotDB db, @CurrentPrincipal Contact principal)
    {
        require(var("globalAdmin", principal.isGlobalAdmin()));
    }
    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db)
    {
        // TODO: worker, pool and other cluster information
        var("workers", app().getProcessor().getWorkerCoordinator().getWorkers());
        var("cluster_info", app().getProcessor().getProcessingPoolCoordinator().info());
        // list sites
        List<Site> sites = var("sites", db.listSites());
        // list global admins
        var("globalAdmins", db.getGlobalSetting(GlobalSetting.NAME.GLOBAL_ADMINS).getParameters().keySet().stream().map((id) -> db.getContact(UUID.fromString(id))).filter((c) -> c != null).collect(Collectors.toList()));
        // list all admins
        List<Contact> allAdmins = new LinkedList<Contact>();
        for (Site site : sites)
        {
            if (! site.isDisabled())
            {
                for (Contact contact : db.listContacts(site.getId()))
                {
                    if (contact.hasPermission("ui.admin") && (! contact.isGlobalAdmin())) 
                        allAdmins.add(contact);
                }
            }
        }
        var("allAdmins", allAdmins);
        // render
        encode("/global/admin/index");
    }
    
    @Any("/site/id/:id/disable")
    @RequireValidAccessTokenForURL(@Param("access-token"))
    @WithDataAdapter(BergamotDB.class)
    public void disableSite(BergamotDB db, @IsaUUID UUID id) throws Exception
    {
        // get the site 
        Site site = notNull(db.getSite(id));
        logger.warn("Disabling site: " + site.getName() + " (" + site.getId() + ")"); 
        // mark the site as disabled
        site.setDisabled(true);
        db.setSite(site);
        // message the UI cluster to tear down the site
        // TODO:
        /*
        try (BergamotClusterManagerQueue queue = BergamotClusterManagerQueue.open())
        {
            try (RPCClient<ClusterManagerRequest, ClusterManagerResponse, RoutingKey> client = queue.createBergamotClusterManagerRPCClient())
            {
                try
                {
                    ClusterManagerResponse response = client.publish(new DeinitSite(site.getId(), site.getName())).get(30, TimeUnit.SECONDS);
                    if (response instanceof DeinitedSite)
                    {
                        logger.info("Deinitialised site with UI cluster");
                    }
                }
                catch (Exception e)
                {
                    logger.error("Failed to deinitialise site with UI cluster");
                }
            }
        }*/
        redirect(path("/global/admin/"));
    }
    
    @Any("/site/id/:id/enable")
    @RequireValidAccessTokenForURL(@Param("access-token"))
    @WithDataAdapter(BergamotDB.class)
    public void enableSite(BergamotDB db, @IsaUUID UUID id) throws Exception
    {
        // get the site 
        Site site = notNull(db.getSite(id));
        logger.warn("Enabling site: " + site.getName() + " (" + site.getId() + ")");
        // mark the site as enabled
        site.setDisabled(false);
        db.setSite(site);
        // message the UI cluster to setup the site
        // TODO:
        /*
        try (BergamotClusterManagerQueue queue = BergamotClusterManagerQueue.open())
        {
            try (RPCClient<ClusterManagerRequest, ClusterManagerResponse, RoutingKey> client = queue.createBergamotClusterManagerRPCClient())
            {
                try
                {
                    ClusterManagerResponse response = client.publish(new InitSite(site.getId(), site.getName())).get(30, TimeUnit.SECONDS);
                    if (response instanceof InitedSite)
                    {
                        logger.info("Initialised site with UI cluster");
                    }
                }
                catch (Exception e)
                {
                    logger.error("Failed to initialise site with UI cluster");
                }
            }
        }
        */
        redirect(path("/global/admin/"));
    }
    
    @Any("/site/id/:id/generate-certificates")
    @RequireValidAccessTokenForURL(@Param("access-token"))
    @WithDataAdapter(BergamotDB.class)
    public void generateSiteCertificates(BergamotDB db, @IsaUUID UUID id) throws Exception
    {
        // get the site 
        Site site = notNull(db.getSite(id));
        logger.warn("Generating certificates for site: " + site.getName() + " (" + site.getId() + ")");
        // message the agent manager
        try (BergamotAgentManagerQueue queue = BergamotAgentManagerQueue.open())
        {
            try (RPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey> client = queue.createBergamotAgentManagerRPCClient())
            {
                try
                {
                    AgentManagerResponse response = client.publish(new CreateSiteCA(site.getId(), site.getName())).get(5, TimeUnit.SECONDS);
                    if (response instanceof CreatedSiteCA)
                    {
                        logger.info("Created Bergamot Agent site Certificate Authority");
                    }
                }
                catch (Exception e)
                {
                }
            }
        }
        redirect(path("/global/admin/"));
    }
    
    @Any("/contact/id/:id/disable")
    @RequireValidAccessTokenForURL(@Param("access-token"))
    @WithDataAdapter(BergamotDB.class)
    public void disableContact(BergamotDB db, @IsaUUID UUID id) throws Exception
    {
        Contact contact = notNull(db.getContact(id));
        GlobalSetting globalAdmins = db.getGlobalSetting(GlobalSetting.NAME.GLOBAL_ADMINS);
        globalAdmins.setParameter(contact.getId().toString(), "false");
        db.setGlobalSetting(globalAdmins);
        redirect(path("/global/admin/"));
    }
    
    @Any("/contact/id/:id/enable")
    @RequireValidAccessTokenForURL(@Param("access-token"))
    @WithDataAdapter(BergamotDB.class)
    public void enableContact(BergamotDB db, @IsaUUID UUID id) throws Exception
    {
        Contact contact = notNull(db.getContact(id));
        GlobalSetting globalAdmins = db.getGlobalSetting(GlobalSetting.NAME.GLOBAL_ADMINS);
        globalAdmins.setParameter(contact.getId().toString(), "true");
        db.setGlobalSetting(globalAdmins);
        redirect(path("/global/admin/"));
    }
    
    @Any("/contact/id/:id/remove")
    @RequireValidAccessTokenForURL(@Param("access-token"))
    @WithDataAdapter(BergamotDB.class)
    public void removeContact(BergamotDB db, @IsaUUID UUID id) throws Exception
    {
        Contact contact = notNull(db.getContact(id));
        GlobalSetting globalAdmins = db.getGlobalSetting(GlobalSetting.NAME.GLOBAL_ADMINS);
        globalAdmins.removeParameter(contact.getId().toString());
        db.setGlobalSetting(globalAdmins);
        redirect(path("/global/admin/"));
    }
    
    @Any("/contact/add")
    @RequireValidAccessTokenForURL(@Param("access-token"))
    @WithDataAdapter(BergamotDB.class)
    public void addContact(BergamotDB db, @IsaUUID @Param("id") UUID id) throws Exception
    {
        Contact contact = notNull(db.getContact(id));
        GlobalSetting globalAdmins = db.getGlobalSetting(GlobalSetting.NAME.GLOBAL_ADMINS);
        globalAdmins.setParameter(contact.getId().toString(), "true");
        db.setGlobalSetting(globalAdmins);
        redirect(path("/global/admin/"));
    }
}
