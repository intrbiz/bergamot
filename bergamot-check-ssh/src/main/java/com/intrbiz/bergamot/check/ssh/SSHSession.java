package com.intrbiz.bergamot.check.ssh;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.intrbiz.util.Hash;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHSession
{
    private final JSch jsch;
    
    private final Session session;
    
    public SSHSession(JSch jsch, Session session)
    {
        this.jsch = jsch;
        this.session = session;
    }
    
    public String hostId()
    {
        return session.getHostKey().getFingerPrint(this.jsch);
    }
    
    public ExecStat exec(String command)
    {
        try
        {
            ChannelExec exec = (ChannelExec) this.session.openChannel("exec");
            // execute
            exec.setCommand(command);
            // input
            if (command != null && command.length() > 0)
                exec.setInputStream(new ByteArrayInputStream(Hash.asUTF8(command)));
            // outputs
            ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
            InputStream stdOutStream = exec.getInputStream();
            InputStream stdErrStream = exec.getExtInputStream();
            try
            {
                exec.connect();
                // process the streams
                while (! exec.isClosed())
                {
                    delicateCopy(stdOutStream, stdOut);
                    delicateCopy(stdErrStream, stdErr);
                }
                delicateCopy(stdOutStream, stdOut);
                delicateCopy(stdErrStream, stdErr);
                // should be done now
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
        catch (Exception e)
        {
            throw new SSHException(e);
        }
    }
    
    private static final void delicateCopy(InputStream from, ByteArrayOutputStream to) throws IOException
    {
        byte[] buf = new byte[1024];
        int r;
        if (from.available() > 0)
        {
            r = from.read(buf);
            if (r > 0) to.write(buf, 0, r);
        }
    }
}
