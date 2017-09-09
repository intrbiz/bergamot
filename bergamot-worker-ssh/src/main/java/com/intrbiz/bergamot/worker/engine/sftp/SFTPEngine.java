package com.intrbiz.bergamot.worker.engine.sftp;

import com.intrbiz.bergamot.check.ssh.SSHChecker;
import com.intrbiz.bergamot.worker.engine.AbstractEngine;
import com.intrbiz.bergamot.worker.engine.sftp.ScriptedSFTPExecutor;

/**
 * Execute checks via SSH (including SFTP)
 */
public class SFTPEngine extends AbstractEngine
{
    public static final String NAME = "sftp";
    
    private SSHChecker checker;

    public SFTPEngine()
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
            this.addExecutor(new ScriptedSFTPExecutor());
        }
    }
    
    @Override
    protected void startEngineServices() throws Exception
    {
        this.checker = new SSHChecker();
    }
}
