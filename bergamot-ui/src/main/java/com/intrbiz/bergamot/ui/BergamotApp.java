package com.intrbiz.bergamot.ui;

import java.io.File;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.bergamot.BergamotDaemon;
import com.intrbiz.bergamot.config.BergamotDaemonCfg;
import com.intrbiz.bergamot.ui.api.APIRouter;
import com.intrbiz.bergamot.ui.api.AlertsAPIRouter;
import com.intrbiz.bergamot.ui.api.ClusterAPIRouter;
import com.intrbiz.bergamot.ui.api.CommandAPIRouter;
import com.intrbiz.bergamot.ui.api.GroupAPIRouter;
import com.intrbiz.bergamot.ui.api.HostAPIRouter;
import com.intrbiz.bergamot.ui.api.LocationAPIRouter;
import com.intrbiz.bergamot.ui.api.ResourceAPIRouter;
import com.intrbiz.bergamot.ui.api.ServiceAPIRouter;
import com.intrbiz.bergamot.ui.api.TimePeriodAPIRouter;
import com.intrbiz.bergamot.ui.api.TrapAPIRouter;
import com.intrbiz.bergamot.ui.router.ClusterRouter;
import com.intrbiz.bergamot.ui.router.DashboardRouter;
import com.intrbiz.bergamot.ui.router.GroupsRouter;
import com.intrbiz.bergamot.ui.router.HostRouter;
import com.intrbiz.bergamot.ui.router.LocationRouter;
import com.intrbiz.bergamot.ui.router.ResourceRouter;
import com.intrbiz.bergamot.ui.router.ServiceRouter;
import com.intrbiz.bergamot.ui.router.TrapRouter;

/**
 * A very basic Bergamot web interface
 */
public class BergamotApp extends BalsaApplication
{
    private Bergamot bergamot;
    
    @Override
    protected void setup() throws Exception
    {
        // setup our internal daemon
        this.bergamot = new BergamotDaemon();
        this.bergamot.configure(BergamotDaemonCfg.read(new File(System.getProperty("bergamot.cfg","/etc/bergamot.xml"))));
        // Setup the application routers
        router(new DashboardRouter());
        router(new GroupsRouter());
        router(new ServiceRouter());
        router(new HostRouter());
        router(new LocationRouter());
        router(new TrapRouter());
        router(new ClusterRouter());
        router(new ResourceRouter());
        // API
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
    }
    
    public Bergamot getBergamot()
    {
        return this.bergamot;
    }
    
    public static void main(String[] args) throws Exception
    {
        BergamotApp bergamotApp = new BergamotApp();
        bergamotApp.start();
        bergamotApp.bergamot.start();
    }
}
