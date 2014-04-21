package com.intrbiz.bergamot.ui;

import java.io.File;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.bergamot.config.BergamotCfg;
import com.intrbiz.bergamot.ui.router.AppRouter;

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
        this.bergamot.configure(BergamotCfg.read(new File("bergamot.xml")));
        // Setup the application routers
        router(new AppRouter());
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
