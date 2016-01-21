package com.intrbiz.bergamot.worker.engine.ssh;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;

/**
 * Execute checks over SSH
 */
public class SSHExecutor extends AbstractExecutor<SSHEngine>
{
    private Logger logger = Logger.getLogger(SSHExecutor.class);

    public SSHExecutor()
    {
        super();
    }

    /**
     * Only execute Checks where the engine == "ssh"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return SSHEngine.NAME.equalsIgnoreCase(task.getEngine());
    }

    @Override
    public void execute(ExecuteCheck executeCheck)
    {
        // TODO
    }
}
