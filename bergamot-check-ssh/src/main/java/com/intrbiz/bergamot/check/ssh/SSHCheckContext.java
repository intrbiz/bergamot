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
    
    protected SSHCheckContext applyDefaultConfiguration(Session session)
    {
        session.setConfig("StrictHostKeyChecking", "no");
        return this;
    }
    
    public SSHCheckContext addIdentity(String privateKeyContents, String publicKeyContents)
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
        return this;
    }
    
    public SSHCheckContext setPassword(String password)
    {
        this.password = password;
        return this;
    }
    
    public SSHCheckContext connect(String username, String host, Consumer<SSHSessionContext> onConnected)
    {
        this.connect(username, host, 22, onConnected);
        return this;
    }
    
    public SSHCheckContext connect(String username, String host, String port, Consumer<SSHSessionContext> onConnected)
    {
        return this.connect(username, host, Integer.parseInt(port), onConnected);
    }
    
    public SSHCheckContext connect(String username, String host, int port, Consumer<SSHSessionContext> onConnected)
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
                onConnected.accept(new SSHSessionContext(this.jsch, session));
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
        return this;
    }
}
