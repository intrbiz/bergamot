package com.intrbiz.bergamot.worker.engine.ssh.util;

import com.intrbiz.Util;
import com.intrbiz.bergamot.check.ssh.SSHCheckContext;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

public class SSHCheckUtil
{
    public static void validateSSHParameters(ExecuteCheck task)
    {
        if (Util.isEmpty(task.getParameter("host"))) throw new RuntimeException("The 'host' parameter must be given");
        if (Util.isEmpty(task.getParameter("private_key")) && Util.isEmpty(task.getParameter("password"))) throw new RuntimeException("The SSH 'private_key' parameter must be given");
        if (Util.isEmpty(task.getParameter("public_key")) && Util.isEmpty(task.getParameter("password"))) throw new RuntimeException("The SSH 'public_key' parameter must be given");
        if (Util.isEmpty(task.getParameter("password")) && Util.isEmpty(task.getParameter("public_key")) && Util.isEmpty(task.getParameter("private_key"))) throw new RuntimeException("No authentication details provided, please use: 'private_key' and 'public_key' or 'password'");
    }
    
    public static String getSSHUsername(ExecuteCheck task)
    {
        return task.getParameter("username", "bergamot");
    }
    
    public static String getSSHHost(ExecuteCheck task)
    {
        return task.getParameter("host");
    }
    
    public static int getSSHPort(ExecuteCheck task)
    {
        return task.getIntParameter("port", 22);
    }
    
    public static void setupSSHCheckContext(ExecuteCheck task, SSHCheckContext context)
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
