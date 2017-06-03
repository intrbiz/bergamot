package com.intrbiz.bergamot.worker.engine.ssh;

import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;

public abstract class BaseSSHExecutor extends AbstractExecutor<SSHEngine>
{
    public BaseSSHExecutor()
    {
        super();
    }
    
    /**
     * Only execute Checks where the engine == "ssh" or engine == "sftp"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return SSHEngine.SSH_NAME.equalsIgnoreCase(task.getEngine()) ||
               SSHEngine.SFTP_NAME.equalsIgnoreCase(task.getEngine());
    }
}
