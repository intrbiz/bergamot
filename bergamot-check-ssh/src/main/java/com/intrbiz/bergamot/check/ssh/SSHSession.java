package com.intrbiz.bergamot.check.ssh;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.intrbiz.util.Hash;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHSession
{
    private final Session session;
    
    public SSHSession(Session session)
    {
        this.session = session;
    }
    
    
    public ExecStat exec(String command)
    {
        try
        {
            ChannelExec exec = (ChannelExec) this.session.openChannel("exec");
            // input
            if (command != null && command.length() > 0)
                exec.setInputStream(new ByteArrayInputStream(Hash.asUTF8(command)));
            // outputs
            // TODO: limit max buffering
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
            ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
            exec.setOutputStream(stdOut);
            exec.setExtOutputStream(stdErr);
            // execute
            exec.setCommand(command);
            try
            {
                exec.connect();
                // get the exit
                int exit = exec.getExitStatus();
                return new ExecStat(exit, new String(stdOut.toByteArray()), new String(stdErr.toByteArray()));
            }
            finally
            {
                exec.disconnect();
            }
        }
        catch (JSchException e)
        {
            throw new SSHException(e);
        }
    }
}
