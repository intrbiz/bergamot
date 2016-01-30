package com.intrbiz.bergamot.worker.engine.ssh;

import com.intrbiz.bergamot.check.ssh.SSHChecker;
import com.intrbiz.bergamot.worker.engine.AbstractEngine;

/**
 * Execute checks via SSH
 */
public class SSHEngine extends AbstractEngine
{
    public static final String NAME = "ssh";
    
    private SSHChecker checker;

    public SSHEngine()
    {
        super(NAME);
    }
    
    public SSHChecker getChecker()
    {
        return this.checker;
    }

    @Override
    protected void configure() throws Exception
    {
        super.configure();
        if (this.executors.isEmpty())
        {
            this.addExecutor(new NagiosSSHExecutor());
            this.addExecutor(new ScriptedSSHExecutor());
        }
    }
    
    @Override
    protected void startEngineServices() throws Exception
    {
        this.checker = new SSHChecker();
    }
}
