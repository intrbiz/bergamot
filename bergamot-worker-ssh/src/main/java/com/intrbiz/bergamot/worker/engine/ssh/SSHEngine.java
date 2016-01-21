package com.intrbiz.bergamot.worker.engine.ssh;

import com.intrbiz.bergamot.worker.engine.AbstractEngine;

/**
 * Execute checks via SSH
 */
public class SSHEngine extends AbstractEngine
{
    public static final String NAME = "ssh";

    public SSHEngine()
    {
        super(NAME);
    }

    @Override
    protected void configure() throws Exception
    {
        super.configure();
        if (this.executors.isEmpty())
        {
            this.addExecutor(new SSHExecutor());
        }
    }
}
