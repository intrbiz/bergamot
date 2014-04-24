package com.intrbiz.bergamot.ui;

import java.io.File;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.bergamot.config.BergamotCfg;
import com.intrbiz.bergamot.ui.router.DashboardRouter;
import com.intrbiz.bergamot.ui.router.HostGroupsRouter;
import com.intrbiz.bergamot.ui.router.HostRouter;
import com.intrbiz.bergamot.ui.router.LocationRouter;
import com.intrbiz.bergamot.ui.router.ServiceGroupsRouter;
import com.intrbiz.bergamot.ui.router.ServiceRouter;

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
        this.bergamot = new Bergamot();
        this.bergamot.configure(BergamotCfg.read(new File(System.getProperty("bergamot.cfg","/etc/bergamot.xml"))));
        // Setup the application routers
        router(new DashboardRouter());
        router(new ServiceGroupsRouter());
        router(new ServiceRouter());
        router(new HostGroupsRouter());
        router(new HostRouter());
        router(new LocationRouter());
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
