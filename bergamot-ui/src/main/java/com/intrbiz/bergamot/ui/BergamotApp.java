package com.intrbiz.bergamot.ui;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.result.DefaultResultProcessor;
import com.intrbiz.bergamot.result.ResultProcessor;
import com.intrbiz.bergamot.scheduler.Scheduler;
import com.intrbiz.bergamot.scheduler.WheelScheduler;
import com.intrbiz.bergamot.ui.action.ExecuteCheckAction;
import com.intrbiz.bergamot.ui.action.SchedulerActions;
import com.intrbiz.bergamot.ui.login.LoginRouter;
import com.intrbiz.bergamot.ui.router.ClusterRouter;
import com.intrbiz.bergamot.ui.router.DashboardRouter;
import com.intrbiz.bergamot.ui.router.GroupsRouter;
import com.intrbiz.bergamot.ui.router.HostRouter;
import com.intrbiz.bergamot.ui.router.LocationRouter;
import com.intrbiz.bergamot.ui.router.ResourceRouter;
import com.intrbiz.bergamot.ui.router.ServiceRouter;
import com.intrbiz.bergamot.ui.router.TrapRouter;
import com.intrbiz.bergamot.ui.security.BergamotSecurityEngine;
import com.intrbiz.bergamot.updater.UpdateServer;
import com.intrbiz.data.DataManager;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.rabbit.RabbitPool;
import com.intrbiz.util.pool.database.DatabasePool;

/**
 * A very basic Bergamot web interface
 */
public class BergamotApp extends BalsaApplication
{
    private ResultProcessor resultProcessor;
    
    private Scheduler scheduler;
    
    private UpdateServer updateServer;
    
    @Override
    protected void setup() throws Exception
    {
        // TODO - read a config file
        // setup the queues
        QueueManager.getInstance().registerDefaultBroker(new RabbitPool("amqp://127.0.0.1"));
        // setup the database
        DataManager.getInstance().registerDefaultServer(DatabasePool.Default.create(org.postgresql.Driver.class, "jdbc:postgresql://127.0.0.1/bergamot", "bergamot", "bergamot"));
        try (BergamotDB db = BergamotDB.connect())
        {
            System.out.println("Database: " + db.getName() + " " + db.getVersion());
        }
        // setup the critical Bergamot components,
        // these will probably move to external daemons
        // at some point, or at least will be managed 
        // better
        // the result processor
        this.resultProcessor = new DefaultResultProcessor();
        // the scheduler
        this.scheduler = new WheelScheduler();
        // websocket update server
        this.updateServer = new UpdateServer(Integer.getInteger("bergamot.websocket.port", 8081));
        // security engine
        securityEngine(new BergamotSecurityEngine());
        // some actions
        action(new ExecuteCheckAction());
        action(new SchedulerActions());
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
        // API
        /*
        router(new APIRouter());
        router(new AlertsAPIRouter());
        router(new LocationAPIRouter());
        router(new GroupAPIRouter());
        router(new HostAPIRouter());
        router(new ClusterAPIRouter());
        router(new ServiceAPIRouter());
        router(new TrapAPIRouter());
        router(new ResourceAPIRouter());
        router(new TimePeriodAPIRouter());
        router(new CommandAPIRouter());
        router(new ContactAPIRouter());
        router(new TeamAPIRouter());
        */
    }
    
    @Override
    protected void startApplication() throws Exception
    {
        this.resultProcessor.start();
        this.scheduler.start();
        this.updateServer.start();
    }
    
    public ResultProcessor getResultProcessor()
    {
        return resultProcessor;
    }

    public Scheduler getScheduler()
    {
        return scheduler;
    }

    public UpdateServer getUpdateServer()
    {
        return updateServer;
    }

    public void setUpdateServer(UpdateServer updateServer)
    {
        this.updateServer = updateServer;
    }

    public static void main(String[] args) throws Exception
    {
        BergamotApp bergamotApp = new BergamotApp();
        bergamotApp.start();
    }
}
