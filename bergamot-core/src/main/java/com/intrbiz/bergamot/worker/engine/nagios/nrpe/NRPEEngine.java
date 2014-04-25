package com.intrbiz.bergamot.worker.engine.nagios.nrpe;

import com.intrbiz.bergamot.worker.engine.AbstractEngine;

/**
 * A dedicated pure Java NRPE check execution engine
 */
public class NRPEEngine extends AbstractEngine
{
    public static final String NAME = "nrpe";

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
    }
}
