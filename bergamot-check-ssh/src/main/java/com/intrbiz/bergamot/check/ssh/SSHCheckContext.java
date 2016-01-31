package com.intrbiz.bergamot.check.ssh;

import java.util.function.Consumer;

import com.intrbiz.util.Hash;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHCheckContext
{
    private final Consumer<Throwable> onError;
    
    private JSch jsch;
    
    private String password = null;
    
    public SSHCheckContext(Consumer<Throwable> onError)
    {
        this.onError = onError;
        this.jsch = new JSch();
    }
    
    protected void applyDefaultConfiguration(Session session)
    {
        session.setConfig("StrictHostKeyChecking", "no");
    }
    
    public void addIdentity(String privateKeyContents, String publicKeyContents)
    {
        try
        {
            String name = Hash.toHex(Hash.sha256(Hash.asUTF8(privateKeyContents)));
            this.jsch.addIdentity(name, Hash.asUTF8(privateKeyContents), Hash.asUTF8(publicKeyContents), null);
        }
        catch (JSchException e)
        {
            this.onError.accept(new SSHException(e));
        }
    }
    
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public void connect(String username, String host, Consumer<SSHSession> onConnected)
    {
        this.connect(username, host, 22, onConnected);
    }
    
    public void connect(String username, String host, int port, Consumer<SSHSession> onConnected)
    {
        try
        {
            Session session = null;
            try
            {
                session = this.jsch.getSession(username, host, port);
                this.applyDefaultConfiguration(session);
                if (this.password != null)
                {
                    session.setPassword(this.password);
                }
                session.connect();
                onConnected.accept(new SSHSession(this.jsch, session));
            }
            finally
            {
                if (session != null) session.disconnect();
            }
        }
        catch (Throwable t)
        {
            this.onError.accept(t);
        }
    }
}
