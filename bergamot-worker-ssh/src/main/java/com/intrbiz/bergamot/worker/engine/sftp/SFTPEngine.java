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
        super(NAME,
                new ScriptedSFTPExecutor());
        // setup checker
        this.checker = new SSHChecker();
    }
    
    public SSHChecker getChecker()
    {
        return this.checker;
    }
}
