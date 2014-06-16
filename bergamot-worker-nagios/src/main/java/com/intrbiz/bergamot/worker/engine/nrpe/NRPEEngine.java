package com.intrbiz.bergamot.worker.engine.nrpe;

import com.intrbiz.bergamot.nrpe.NRPEPoller;
import com.intrbiz.bergamot.worker.engine.AbstractEngine;

/**
 * A dedicated pure Java NRPE check execution engine
 */
public class NRPEEngine extends AbstractEngine
{
    public static final String NAME = "nrpe";
    
    private NRPEPoller poller;

    public NRPEEngine()
    {
        super(NAME); 
    }

    @Override
    protected void configure() throws Exception
    {
        super.configure();
        if (this.executors.isEmpty())
        {
            this.addExecutor(new NRPEExecutor());
        }
        // setup the NRPE poller
        this.poller = new NRPEPoller();
    }
    
    public NRPEPoller getPoller()
    {
        return this.poller;
    }
}
