package com.intrbiz.bergamot.worker.engine.nagios;

import com.intrbiz.bergamot.worker.engine.AbstractEngine;

/**
 * Execute 
 */
public class NagiosEngine extends AbstractEngine
{
    public static final String NAME = "nagios";

    public NagiosEngine()
    {
        super(NAME);
    }

    @Override
    protected void configure() throws Exception
    {
        super.configure();
        if (this.executors.isEmpty())
        {
            this.addExecutor(new NagiosExecutor());
        }
    }
}
