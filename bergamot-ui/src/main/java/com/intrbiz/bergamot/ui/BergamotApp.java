package com.intrbiz.bergamot.ui;

import java.util.UUID;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.engine.impl.session.HazelcastSessionEngine;
import com.intrbiz.balsa.util.Util;
import com.intrbiz.bergamot.cluster.ClusterManager;
import com.intrbiz.bergamot.config.UICfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.action.BergamotAgentActions;
import com.intrbiz.bergamot.ui.action.CheckActions;
import com.intrbiz.bergamot.ui.action.ConfigChangeActions;
import com.intrbiz.bergamot.ui.action.ContactActions;
import com.intrbiz.bergamot.ui.action.DispatchResultAction;
import com.intrbiz.bergamot.ui.action.ExecuteCheckAction;
import com.intrbiz.bergamot.ui.action.SchedulerActions;
import com.intrbiz.bergamot.ui.action.TeamActions;
import com.intrbiz.bergamot.ui.action.TimePeriodActions;
import com.intrbiz.bergamot.ui.api.APIRouter;
import com.intrbiz.bergamot.ui.api.AgentAPIRouter;
import com.intrbiz.bergamot.ui.api.AlertsAPIRouter;
import com.intrbiz.bergamot.ui.api.ClusterAPIRouter;
import com.intrbiz.bergamot.ui.api.CommandAPIRouter;
import com.intrbiz.bergamot.ui.api.CommentsAPIRouter;
import com.intrbiz.bergamot.ui.api.ConfigAPIRouter;
import com.intrbiz.bergamot.ui.api.ContactAPIRouter;
import com.intrbiz.bergamot.ui.api.DowntimeAPIRouter;
import com.intrbiz.bergamot.ui.api.GroupAPIRouter;
import com.intrbiz.bergamot.ui.api.HostAPIRouter;
import com.intrbiz.bergamot.ui.api.LamplighterAPIRouter;
import com.intrbiz.bergamot.ui.api.LocationAPIRouter;
import com.intrbiz.bergamot.ui.api.MetricsAPIRouter;
import com.intrbiz.bergamot.ui.api.ResourceAPIRouter;
import com.intrbiz.bergamot.ui.api.ServiceAPIRouter;
import com.intrbiz.bergamot.ui.api.StatsAPIRouter;
import com.intrbiz.bergamot.ui.api.TeamAPIRouter;
import com.intrbiz.bergamot.ui.api.TestAPIRouter;
import com.intrbiz.bergamot.ui.api.TimePeriodAPIRouter;
import com.intrbiz.bergamot.ui.api.TrapAPIRouter;
import com.intrbiz.bergamot.ui.api.UtilAPIRouter;
import com.intrbiz.bergamot.ui.express.BergamotUpdateURL;
import com.intrbiz.bergamot.ui.router.AboutRouter;
import com.intrbiz.bergamot.ui.router.ClusterRouter;
import com.intrbiz.bergamot.ui.router.CommandRouter;
import com.intrbiz.bergamot.ui.router.ContactRouter;
import com.intrbiz.bergamot.ui.router.DashboardRouter;
import com.intrbiz.bergamot.ui.router.ErrorRouter;
import com.intrbiz.bergamot.ui.router.GroupsRouter;
import com.intrbiz.bergamot.ui.router.HostRouter;
import com.intrbiz.bergamot.ui.router.LocationRouter;
import com.intrbiz.bergamot.ui.router.LoginRouter;
import com.intrbiz.bergamot.ui.router.ProfileRouter;
import com.intrbiz.bergamot.ui.router.ResourceRouter;
import com.intrbiz.bergamot.ui.router.ServiceRouter;
import com.intrbiz.bergamot.ui.router.StatsRouter;
import com.intrbiz.bergamot.ui.router.TeamRouter;
import com.intrbiz.bergamot.ui.router.TimePeriodRouter;
import com.intrbiz.bergamot.ui.router.TrapRouter;
import com.intrbiz.bergamot.ui.router.admin.AdminRouter;
import com.intrbiz.bergamot.ui.router.admin.ClusterAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.CommandAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.ConfigAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.ConfigChangeAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.ContactAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.GroupAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.HostAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.LocationAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.ResourceAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.ServiceAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.SiteAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.TeamAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.TimePeriodAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.TrapAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.UtilsAdminRouter;
import com.intrbiz.bergamot.ui.router.agent.AgentRouter;
import com.intrbiz.bergamot.ui.security.BergamotSecurityEngine;
import com.intrbiz.bergamot.updater.UpdateServer;
import com.intrbiz.configuration.Configurable;
import com.intrbiz.crypto.SecretKey;
import com.intrbiz.data.DataManager;
import com.intrbiz.data.cache.HazelcastCacheProvider;
import com.intrbiz.gerald.Gerald;
import com.intrbiz.lamplighter.data.LamplighterDB;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.rabbit.RabbitPool;
import com.intrbiz.util.pool.database.DatabasePool;

/**
 * A very basic Bergamot web interface
 */
public class BergamotApp extends BalsaApplication implements Configurable<UICfg>
{   
    public static final class VERSION
    {
        public static final String NUMBER = "2.0.0";
        
        public static final String CODE_NAME = "Yellow Sun";
    }
    
    private UICfg config;
    
    private ClusterManager clusterManager;
    
    private UpdateServer updateServer;
    
    private final UUID id = UUID.randomUUID();
    
    public BergamotApp()
    {
        super();
    }
    
    public UUID id()
    {
        return id;
    }
    
    @Override
    public void configure(UICfg cfg) throws Exception
    {
        this.config = cfg;
    }

    @Override
    public UICfg getConfiguration()
    {
        return this.config;
    }

    @Override
    protected void setup() throws Exception
    {
        // TODO: Don't bother sending metric yet
        // Setup Gerald - Service name: Bergamot.UI, send every minute
        // Gerald.theMole().from(this.getInstanceName()).period(1, TimeUnit.MINUTES);
        // task engine
        /*
         * TODO: disable the shared task engine as we are getting issues with 
         * serialising Apache Log4J Loggers
         * taskEngine(new HazelcastTaskEngine());
         */
        // session engine
        sessionEngine(new HazelcastSessionEngine());
        // security engine
        securityEngine(new BergamotSecurityEngine());
        // setup the application security key
        if (! Util.isEmpty(this.getConfiguration().getSecurityKey()))
        {
            // set the key
            this.getSecurityEngine().applicationKey(SecretKey.fromString(this.getConfiguration().getSecurityKey()));
        }
        // setup ClusterManager to manage our critical
        // resources across the cluster
        this.clusterManager = new ClusterManager();
        // websocket update server
        this.updateServer = new UpdateServer(Integer.getInteger("bergamot.websocket.port", 8081));
        // express functions
        immutableFunction(new BergamotUpdateURL());
        // some actions
        action(new ExecuteCheckAction());
        action(new SchedulerActions());
        action(new DispatchResultAction());
        action(new TimePeriodActions());
        action(new TeamActions());
        action(new ContactActions());
        action(new ConfigChangeActions());
        action(new CheckActions());
        action(new BergamotAgentActions());
        // Setup the application routers
        router(new ErrorRouter());
        router(new LoginRouter());
        router(new DashboardRouter());
        router(new GroupsRouter());
        router(new ServiceRouter());
        router(new HostRouter());
        router(new LocationRouter());
        router(new TrapRouter());
        router(new ClusterRouter());
        router(new ResourceRouter());
        router(new TeamRouter());
        router(new ContactRouter());
        router(new TimePeriodRouter());
        router(new ProfileRouter());
        router(new StatsRouter());
        router(new CommandRouter());
        // Agent
        router(new AgentRouter());
        // About
        router(new AboutRouter());
        // Admin
        router(new AdminRouter());
        router(new ContactAdminRouter());
        router(new TeamAdminRouter());
        router(new TimePeriodAdminRouter());
        router(new CommandAdminRouter());
        router(new LocationAdminRouter());
        router(new GroupAdminRouter());
        router(new HostAdminRouter());
        router(new ServiceAdminRouter());
        router(new TrapAdminRouter());
        router(new ClusterAdminRouter());
        router(new ResourceAdminRouter());
        router(new ConfigChangeAdminRouter());
        router(new ConfigAdminRouter());
        router(new SiteAdminRouter());
        router(new UtilsAdminRouter());
        // API
        router(new APIRouter());
        router(new MetricsAPIRouter());
        router(new AlertsAPIRouter());
        router(new HostAPIRouter());
        router(new LocationAPIRouter());
        router(new GroupAPIRouter());
        router(new ClusterAPIRouter());
        router(new ServiceAPIRouter());
        router(new TrapAPIRouter());
        router(new ResourceAPIRouter());
        router(new TimePeriodAPIRouter());
        router(new CommandAPIRouter());
        router(new ContactAPIRouter());
        router(new TeamAPIRouter());
        router(new TestAPIRouter());
        router(new CommentsAPIRouter());
        router(new DowntimeAPIRouter());
        router(new ConfigAPIRouter());
        router(new StatsAPIRouter());
        router(new UtilAPIRouter());
        router(new AgentAPIRouter());
        router(new LamplighterAPIRouter());
    }
    
    @Override
    protected void startApplication() throws Exception
    {
        // setup the database
        BergamotDB.install();
        try (BergamotDB db = BergamotDB.connect())
        {
            System.out.println("Database module: " + db.getName() + " " + db.getVersion());
        }
        LamplighterDB.install();
        try (LamplighterDB db = LamplighterDB.connect())
        {
            System.out.println("Database module: " + db.getName() + " " + db.getVersion());
        }
        // don't bother starting scheduler etc on ui only nodes
        if (!Boolean.getBoolean("bergamot.ui.only"))
        {
            // start the cluster manager
            this.clusterManager.start(this.getInstanceName());
            // register sites with the cluster manager
            try (BergamotDB db = BergamotDB.connect())
            {
                for (Site site : db.listSites())
                {
                    this.clusterManager.registerSite(site);
                }
            }
        }
        // start the update websocket server
        this.updateServer.start();
        // Start Gerald
        Gerald.theMole().start();
    }
    
    public ClusterManager getClusterManager()
    {
        return this.clusterManager;
    }

    public UpdateServer getUpdateServer()
    {
        return updateServer;
    }

    public static void main(String[] args) throws Exception
    {
        try
        {
            // read config
            UICfg config = UICfg.loadConfiguration();
            System.out.println("Using configuration: ");
            System.out.println(config.toString());
            // compile database
            System.out.println("Compiling DB");
            BergamotDB.load();
            LamplighterDB.load();
            // setup the cache
            System.out.println("Setting up Hazelcast");
            DataManager.get().registerCacheProvider("hazelcast", new HazelcastCacheProvider(BergamotApp.getApplicationInstanceName()));
            DataManager.get().registerDefaultCacheProvider(DataManager.get().cacheProvider("hazelcast"));
            // setup the queue manager
            System.out.println("Setting up RabbitMQ");
            QueueManager.getInstance().registerDefaultBroker(new RabbitPool(config.getBroker().getUrl(), config.getBroker().getUsername(), config.getBroker().getPassword()));
            // setup data manager
            System.out.println("Setting up PostgreSQL");
            DataManager.getInstance().registerDefaultServer(DatabasePool.Default.with().postgresql().url(config.getDatabase().getUrl()).username(config.getDatabase().getUsername()).password(config.getDatabase().getPassword()).build());
            // start the app
            System.out.println("Starting Bergamot UI");
            BergamotApp bergamotApp = new BergamotApp();
            bergamotApp.configure(config);
            bergamotApp.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    public static String getApplicationInstanceName()
    {
        return BalsaApplication.getApplicationInstanceName(BergamotApp.class);
    }
}
