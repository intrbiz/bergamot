package com.intrbiz.bergamot.ui;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.engine.impl.session.HazelcastSessionEngine;
import com.intrbiz.bergamot.cluster.ClusterManager;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.action.DispatchResultAction;
import com.intrbiz.bergamot.ui.action.ExecuteCheckAction;
import com.intrbiz.bergamot.ui.action.SchedulerActions;
import com.intrbiz.bergamot.ui.action.TimePeriodActions;
import com.intrbiz.bergamot.ui.api.APIRouter;
import com.intrbiz.bergamot.ui.api.AlertsAPIRouter;
import com.intrbiz.bergamot.ui.api.ClusterAPIRouter;
import com.intrbiz.bergamot.ui.api.CommandAPIRouter;
import com.intrbiz.bergamot.ui.api.CommentsAPIRouter;
import com.intrbiz.bergamot.ui.api.ContactAPIRouter;
import com.intrbiz.bergamot.ui.api.DowntimeAPIRouter;
import com.intrbiz.bergamot.ui.api.GroupAPIRouter;
import com.intrbiz.bergamot.ui.api.HostAPIRouter;
import com.intrbiz.bergamot.ui.api.LocationAPIRouter;
import com.intrbiz.bergamot.ui.api.MetricsAPIRouter;
import com.intrbiz.bergamot.ui.api.ResourceAPIRouter;
import com.intrbiz.bergamot.ui.api.ServiceAPIRouter;
import com.intrbiz.bergamot.ui.api.TeamAPIRouter;
import com.intrbiz.bergamot.ui.api.TestAPIRouter;
import com.intrbiz.bergamot.ui.api.TimePeriodAPIRouter;
import com.intrbiz.bergamot.ui.api.TrapAPIRouter;
import com.intrbiz.bergamot.ui.login.LoginRouter;
import com.intrbiz.bergamot.ui.router.ClusterRouter;
import com.intrbiz.bergamot.ui.router.ContactRouter;
import com.intrbiz.bergamot.ui.router.DashboardRouter;
import com.intrbiz.bergamot.ui.router.GroupsRouter;
import com.intrbiz.bergamot.ui.router.HostRouter;
import com.intrbiz.bergamot.ui.router.LocationRouter;
import com.intrbiz.bergamot.ui.router.ResourceRouter;
import com.intrbiz.bergamot.ui.router.ServiceRouter;
import com.intrbiz.bergamot.ui.router.TeamRouter;
import com.intrbiz.bergamot.ui.router.TimePeriodRouter;
import com.intrbiz.bergamot.ui.router.TrapRouter;
import com.intrbiz.bergamot.ui.security.BergamotSecurityEngine;
import com.intrbiz.bergamot.updater.UpdateServer;
import com.intrbiz.data.DataManager;
import com.intrbiz.data.cache.HazelcastCacheProvider;
import com.intrbiz.data.cache.tiered.TieredCacheProvider;
import com.intrbiz.gerald.Gerald;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.rabbit.RabbitPool;
import com.intrbiz.util.pool.database.DatabasePool;

/**
 * A very basic Bergamot web interface
 */
public class BergamotApp extends BalsaApplication
{    
    private ClusterManager clusterManager;
    
    private UpdateServer updateServer;
    
    private final UUID id = UUID.randomUUID();
    
    public UUID id()
    {
        return id;
    }
    
    @Override
    protected void setup() throws Exception
    {
        // TODO - read a config file
        // Setup Gerald - Service name: Bergamot.UI, send every minute
        Gerald.theMole().from(this.getInstanceName()).period(1, TimeUnit.MINUTES);
        // session engine
        sessionEngine(new HazelcastSessionEngine());
        // security engine
        securityEngine(new BergamotSecurityEngine());
        // setup the cache
        DataManager.get().registerDefaultCacheProvider(new TieredCacheProvider(new HazelcastCacheProvider(this.getInstanceName())));
        // setup the queues
        QueueManager.getInstance().registerDefaultBroker(new RabbitPool("amqp://127.0.0.1"));
        // setup the database 
        DataManager.getInstance().registerDefaultServer(DatabasePool.Default.with().postgresql().url("jdbc:postgresql://127.0.0.1/bergamot").username("bergamot").password("bergamot").build());
        BergamotDB.install();
        try (BergamotDB db = BergamotDB.connect())
        {
            System.out.println("Database: " + db.getName() + " " + db.getVersion());
        }
        // setup ClusterManager to manage our critical
        // resources across the cluster
        this.clusterManager = new ClusterManager();
        // websocket update server
        this.updateServer = new UpdateServer(Integer.getInteger("bergamot.websocket.port", 8081));
        // some actions
        action(new ExecuteCheckAction());
        action(new SchedulerActions());
        action(new DispatchResultAction());
        action(new TimePeriodActions());
        // Setup the application routers
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
    }
    
    @Override
    protected void startApplication() throws Exception
    {
        // Start Gerald
        Gerald.theMole().start();
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
        // start the update websocket server
        this.updateServer.start();
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
            // start the app
            BergamotApp bergamotApp = new BergamotApp();
            bergamotApp.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
