package com.intrbiz.bergamot.worker.engine.ssh;

import com.intrbiz.bergamot.check.ssh.SSHChecker;
import com.intrbiz.bergamot.worker.engine.AbstractEngine;

/**
 * Execute checks via SSH (including SFTP)
 */
public class SSHEngine extends AbstractEngine
{
    public static final String SSH_NAME = "ssh";
    
    public static final String SFTP_NAME = "sftp";
    
    private SSHChecker checker;

    public SSHEngine()
    {
        super(SSH_NAME);
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
            this.addExecutor(new ScriptedSFTPExecutor());
        }
    }
    
    @Override
    protected void startEngineServices() throws Exception
    {
        this.checker = new SSHChecker();
    }
}
