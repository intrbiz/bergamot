package com.intrbiz.bergamot.worker.engine.nrpe;

import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.nrpe.NRPEPoller;
import com.intrbiz.bergamot.worker.engine.AbstractCheckEngine;

/**
 * A dedicated pure Java NRPE check execution engine
 */
public class NRPEEngine extends AbstractCheckEngine
{
    public static final String NAME = "nrpe";
    
    private NRPEPoller poller;

    public NRPEEngine()
    {
        super(BergamotVersion.NAME, NAME, true, new NRPEExecutor());
        // setup the NRPE poller
        this.poller = new NRPEPoller();
    }
    
    public NRPEPoller getPoller()
    {
        return this.poller;
    }
}
