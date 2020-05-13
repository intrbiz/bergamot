package com.intrbiz.bergamot.ui;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.intrbiz.Util;
import com.intrbiz.accounting.AccountingManager;
import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.engine.impl.session.HazelcastSessionEngine;
import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.accounting.consumer.BergamotLoggingConsumer;
import com.intrbiz.bergamot.config.UICfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.processor.BergamotProcessor;
import com.intrbiz.bergamot.ui.action.CheckActions;
import com.intrbiz.bergamot.ui.action.ConfigChangeActions;
import com.intrbiz.bergamot.ui.action.ContactActions;
import com.intrbiz.bergamot.ui.action.DispatchResultAction;
import com.intrbiz.bergamot.ui.action.ExecuteCheckAction;
import com.intrbiz.bergamot.ui.action.SchedulerActions;
import com.intrbiz.bergamot.ui.action.SiteActions;
import com.intrbiz.bergamot.ui.action.TeamActions;
import com.intrbiz.bergamot.ui.action.TimePeriodActions;
import com.intrbiz.bergamot.ui.action.U2FAActions;
import com.intrbiz.bergamot.ui.action.UpdateActions;
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
import com.intrbiz.bergamot.ui.api.ProxyAPIRouter;
import com.intrbiz.bergamot.ui.api.ResourceAPIRouter;
import com.intrbiz.bergamot.ui.api.ServiceAPIRouter;
import com.intrbiz.bergamot.ui.api.StatsAPIRouter;
import com.intrbiz.bergamot.ui.api.TeamAPIRouter;
import com.intrbiz.bergamot.ui.api.TestAPIRouter;
import com.intrbiz.bergamot.ui.api.TimePeriodAPIRouter;
import com.intrbiz.bergamot.ui.api.TrapAPIRouter;
import com.intrbiz.bergamot.ui.api.UtilAPIRouter;
import com.intrbiz.bergamot.ui.express.BergamotCSSVersion;
import com.intrbiz.bergamot.ui.express.BergamotJSVersion;
import com.intrbiz.bergamot.ui.express.BergamotUpdateURL;
import com.intrbiz.bergamot.ui.router.AboutRouter;
import com.intrbiz.bergamot.ui.router.AlertsRouter;
import com.intrbiz.bergamot.ui.router.ClusterRouter;
import com.intrbiz.bergamot.ui.router.CommandRouter;
import com.intrbiz.bergamot.ui.router.ConfigRouter;
import com.intrbiz.bergamot.ui.router.ContactRouter;
import com.intrbiz.bergamot.ui.router.DashboardRouter;
import com.intrbiz.bergamot.ui.router.ErrorRouter;
import com.intrbiz.bergamot.ui.router.GroupsRouter;
import com.intrbiz.bergamot.ui.router.HealthRouter;
import com.intrbiz.bergamot.ui.router.HostRouter;
import com.intrbiz.bergamot.ui.router.LocationRouter;
import com.intrbiz.bergamot.ui.router.LoginRouter;
import com.intrbiz.bergamot.ui.router.ProfileRouter;
import com.intrbiz.bergamot.ui.router.ResourceRouter;
import com.intrbiz.bergamot.ui.router.SLARouter;
import com.intrbiz.bergamot.ui.router.ServiceRouter;
import com.intrbiz.bergamot.ui.router.StatsRouter;
import com.intrbiz.bergamot.ui.router.StatusRouter;
import com.intrbiz.bergamot.ui.router.TeamRouter;
import com.intrbiz.bergamot.ui.router.TimePeriodRouter;
import com.intrbiz.bergamot.ui.router.TrapRouter;
import com.intrbiz.bergamot.ui.router.UIRouter;
import com.intrbiz.bergamot.ui.router.admin.AdminRouter;
import com.intrbiz.bergamot.ui.router.admin.ClusterAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.CommandAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.ConfigAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.ConfigChangeAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.ContactAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.CredentialAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.GroupAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.HostAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.LocationAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.ResourceAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.SecurityDomainAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.ServiceAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.TeamAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.TimePeriodAdminRouter;
import com.intrbiz.bergamot.ui.router.admin.TrapAdminRouter;
import com.intrbiz.bergamot.ui.router.agent.AgentRouter;
import com.intrbiz.bergamot.ui.router.command.CommandEditorRouter;
import com.intrbiz.bergamot.ui.router.global.CreateSiteRouter;
import com.intrbiz.bergamot.ui.router.global.FirstInstallRouter;
import com.intrbiz.bergamot.ui.router.global.GlobalAdminRouter;
import com.intrbiz.bergamot.ui.router.global.GlobalUtilsAdminRouter;
import com.intrbiz.bergamot.ui.router.proxy.ProxyRouter;
import com.intrbiz.bergamot.ui.security.BergamotSecurityEngine;
import com.intrbiz.bergamot.updater.UpdateServer;
import com.intrbiz.configuration.Configurable;
import com.intrbiz.crypto.SecretKey;
import com.intrbiz.data.DataManager;
import com.intrbiz.data.cache.HazelcastCacheProvider;
import com.intrbiz.lamplighter.data.LamplighterDB;
import com.intrbiz.util.pool.database.DatabasePool;

/**
 * The Bergamot web interface
 */
public class BergamotApp extends BalsaApplication implements Configurable<UICfg>
{   
    private static final Logger logger = Logger.getLogger(BergamotApp.class);
    
    public static final String DAEMON_NAME = "bergamot-ui";
    
    public static final class COMPONENTS
    {
        public static final String JS = "v1.6.0";
        
        public static final String CSS = "v1.7.4";
    }
    
    private UICfg config;
    
    private BergamotProcessor processor;
    
    private UpdateServer updateServer;
    
    private final UUID id = UUID.randomUUID();
    
    private CountDownLatch shutdownLatch;
    
    public BergamotApp()
    {
        super();
    }
    
    public UUID id()
    {
        return id;
    }
    
    public BergamotProcessor getProcessor()
    {
        return this.processor;
    }

    public UpdateServer getUpdateServer()
    {
        return updateServer;
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
    
    /**
     * Override the default configuration method
     */
    protected void configureLogging()
    {
        // configure logging to terminal
        Logger root = Logger.getRootLogger();
        root.addAppender(new ConsoleAppender(new PatternLayout("%d [%t] %p %c %x - %m%n")));
        root.setLevel(Level.toLevel(this.config.getLogging().getLevel().toUpperCase()));
    }
    
    protected int getListenerPort(String listenerType, int defaultPort)
    {
        if ("scgi".equalsIgnoreCase(listenerType)) return this.config.getListen().getScgiPort();
        return Integer.getInteger("balsa." + listenerType + ".port", defaultPort);
    }
    
    protected int getListenerThreadCount(String listenerType, int defaultThreadCount)
    {
        if ("scgi".equalsIgnoreCase(listenerType)) return this.config.getListen().getScgiWorkers();
        return Integer.getInteger("balsa." + listenerType + ".workers", Integer.getInteger("balsa.workers", defaultThreadCount));
    }

    @Override
    protected void setupEngines() throws Exception
    {
        // setup data manager
        DataManager.getInstance().registerDefaultServer(
            DatabasePool.Default.with()
                .postgresql()
                .url(this.config.getDatabase().getUrl())
                .username(this.config.getDatabase().getUsername())
                .password(this.config.getDatabase().getPassword())
                .build()
        );
        // setup accounting
        AccountingManager.getInstance().registerConsumer("logger", new BergamotLoggingConsumer());
        AccountingManager.getInstance().bindRootConsumer("logger");
        // TODO: Don't bother sending metric yet
        // Setup Gerald - Service name: Bergamot.UI, send every minute
        // Gerald.theMole().from(this.getInstanceName()).period(1, TimeUnit.MINUTES);
        // session engine
        sessionEngine(new HazelcastSessionEngine((instanceName) -> this.processor.getHazelcast()));
        // task engine
        /*
         * TODO: disable the shared task engine as we are getting issues with 
         * serialising Apache Log4J Loggers
         * taskEngine(new HazelcastTaskEngine());
         */
        // security engine
        securityEngine(new BergamotSecurityEngine());
        // setup the application security key
        if (! Util.isEmpty(this.getConfiguration().getSecurityKey()))
        {
            // set the key
            this.getSecurityEngine().applicationKey(SecretKey.fromString(this.getConfiguration().getSecurityKey()));
        }
        // websocket update server
        this.updateServer = new UpdateServer(this.config.getListen().getWebsocketPort());
    }

    @Override
    protected void setupFunctions() throws Exception
    {
        // express functions
        immutableFunction(new BergamotUpdateURL());
        immutableFunction(new BergamotJSVersion());
        immutableFunction(new BergamotCSSVersion());
    }
    
    @Override
    protected void setupActions() throws Exception
    {
        // some actions
        action(new ExecuteCheckAction());
        action(new SchedulerActions());
        action(new DispatchResultAction());
        action(new TimePeriodActions());
        action(new TeamActions());
        action(new ContactActions());
        action(new ConfigChangeActions());
        action(new CheckActions());
        action(new U2FAActions());
        action(new SiteActions());
        action(new UpdateActions());
    }
    
    @Override
    protected void setupRouters() throws Exception
    {
        // UI filters and defaults
        router(new UIRouter());
        // health check router
        router(new HealthRouter());
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
        router(new ConfigRouter());
        router(new CommandRouter());
        router(new AlertsRouter());
        router(new StatusRouter());
        router(new SLARouter());
        // Agent
        router(new AgentRouter());
        // Proxy
        router(new ProxyRouter());
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
        router(new SecurityDomainAdminRouter());
        router(new CredentialAdminRouter());
        // Command Editor
        router(new CommandEditorRouter());
        // Global Stuff
        router(new FirstInstallRouter());
        router(new GlobalAdminRouter());
        router(new GlobalUtilsAdminRouter());
        router(new CreateSiteRouter());
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
        router(new ProxyAPIRouter());
        router(new LamplighterAPIRouter());
    }
    
    @Override
    protected void startApplicationEarly() throws Exception
    {
        // compile database
        logger.info("Compiling DB");
        BergamotDB.load();
        LamplighterDB.load();
        // setup the database
        BergamotDB.install();
        try (BergamotDB db = BergamotDB.connect())
        {
            logger.info("Database module: " + db.getName() + " " + db.getVersion());
        }
        LamplighterDB.install();
        try (LamplighterDB db = LamplighterDB.connect())
        {
            logger.info("Database module: " + db.getName() + " " + db.getVersion());
        }
        // Connect to ZooKeeper and start Hazelcast
        this.processor = new BergamotProcessor(this.config.getCluster(), this::clusterPanic, DAEMON_NAME, BergamotVersion.fullVersionString());
        // Setup the database cache
        logger.info("Setting up Hazelcast cache");
        DataManager.get().registerCacheProvider("hazelcast", new HazelcastCacheProvider(this.processor.getHazelcast()));
        DataManager.get().registerDefaultCacheProvider(DataManager.get().cacheProvider("hazelcast"));
    }
    
    @Override
    protected void startApplication() throws Exception
    {
        // start the processor
        logger.info("Starting processor");
        this.shutdownLatch = new CountDownLatch(1);
        this.processor.start();
        // start the update websocket server
        this.updateServer.start();
        // Start Gerald
        // Gerald.theMole().start();
    }
    
    protected void clusterPanic(Void v)
    {
        logger.info("Got cluster panic, shutting down!");
        this.stop();
    }
    
    protected void shutdown()
    {
        logger.info("Shutting down processor!");
        this.processor.shutdown();
        this.processor.close();
        this.shutdownLatch.countDown();
    }
    
    public void awaitShutdown()
    {
        try
        {
            this.shutdownLatch.await();
        }
        catch (InterruptedException e)
        {
        }
    }

    public static void main(String[] args) throws Exception
    {
        try
        {
            // read config
            UICfg config = UICfg.loadConfiguration();
            System.out.println("Using configuration: ");
            System.out.println(config.toString());
            // create the application
            BergamotApp bergamotApp = new BergamotApp();
            bergamotApp.configure(config);
            // setup shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Triggering shutdown of Bergamot UI");
                bergamotApp.stop();
            }));
            // start the app
            System.out.println("Starting Bergamot");
            bergamotApp.start();
            // Await shutdown
            bergamotApp.awaitShutdown();
            // Terminate normally
            System.out.println("Exiting Bergamot");
            Thread.sleep(15_000);
            System.exit(0);
        }
        catch (Exception e)
        {
            System.err.println("Failed to start Bergamot UI!");
            e.printStackTrace();
            Thread.sleep(15_000);
            System.exit(1);
        }
    }
    
    public static String getApplicationInstanceName()
    {
        return BalsaApplication.getApplicationInstanceName(BergamotApp.class);
    }
}
