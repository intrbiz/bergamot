package com.intrbiz.bergamot.worker.engine.sftp;

import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.check.ssh.SSHChecker;
import com.intrbiz.bergamot.worker.engine.AbstractCheckEngine;
import com.intrbiz.bergamot.worker.engine.sftp.ScriptedSFTPExecutor;

/**
 * Execute checks via SSH (including SFTP)
 */
public class SFTPEngine extends AbstractCheckEngine
{
    public static final String NAME = "sftp";
    
    private SSHChecker checker;

    public SFTPEngine()
    {
        super(BergamotVersion.NAME, NAME, true,
                new ScriptedSFTPExecutor());
        // setup checker
        this.checker = new SSHChecker();
    }
    
    public SSHChecker getChecker()
    {
        return this.checker;
    }
}
