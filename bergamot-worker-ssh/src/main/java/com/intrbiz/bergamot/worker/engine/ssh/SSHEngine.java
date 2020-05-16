package com.intrbiz.bergamot.worker.engine.ssh;

import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.check.ssh.SSHChecker;
import com.intrbiz.bergamot.worker.engine.AbstractCheckEngine;

/**
 * Execute checks via SSH (including SFTP)
 */
public class SSHEngine extends AbstractCheckEngine
{
    public static final String NAME = "ssh";
    
    private SSHChecker checker;

    public SSHEngine()
    {
        super(BergamotVersion.NAME, NAME, true,
                new NagiosSSHExecutor(),
                new ScriptedSSHExecutor());
        // setup checker
        this.checker = new SSHChecker();
    }
    
    public SSHChecker getChecker()
    {
        return this.checker;
    }
}
