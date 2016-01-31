package com.intrbiz.bergamot.worker.engine.ssh;

import com.intrbiz.Util;
import com.intrbiz.bergamot.check.ssh.SSHCheckContext;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;

public abstract class BaseSSHExecutor extends AbstractExecutor<SSHEngine>
{
    public BaseSSHExecutor()
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
    
    protected void validateSSHParameters(ExecuteCheck task)
    {
        if (Util.isEmpty(task.getParameter("host"))) throw new RuntimeException("The 'host' parameter must be given");
        if (Util.isEmpty(task.getParameter("private_key")) && Util.isEmpty(task.getParameter("password"))) throw new RuntimeException("The SSH 'private_key' parameter must be given");
        if (Util.isEmpty(task.getParameter("public_key")) && Util.isEmpty(task.getParameter("password"))) throw new RuntimeException("The SSH 'public_key' parameter must be given");
        if (Util.isEmpty(task.getParameter("password")) && Util.isEmpty(task.getParameter("public_key")) && Util.isEmpty(task.getParameter("private_key"))) throw new RuntimeException("No authentication details provided, please use: 'private_key' and 'public_key' or 'password'");
    }
    
    protected String getSSHUsername(ExecuteCheck task)
    {
        return task.getParameter("username", "bergamot");
    }
    
    protected String getSSHHost(ExecuteCheck task)
    {
        return task.getParameter("host");
    }
    
    protected int getSSHPort(ExecuteCheck task)
    {
        return task.getIntParameter("port", 22);
    }
    
    protected void setupSSHCheckContext(ExecuteCheck task, SSHCheckContext context)
    {
        if ((! Util.isEmpty(task.getParameter("private_key"))) && (! Util.isEmpty(task.getParameter("public_key"))))
        {
            // setup the identity
            String privateKey = task.getParameter("private_key");
            String publicKey  = task.getParameter("public_key");
            context.addIdentity(privateKey, publicKey);
        }
        else if ((! Util.isEmpty(task.getParameter("password"))))
        {
            // use password
            context.setPassword(task.getParameter("password"));
        }
    }    
}
