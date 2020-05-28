package com.intrbiz.bergamot.check.jmx;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.intrbiz.Util;

public class JMXCheckContext
{    
    private final Consumer<Throwable> onError;
    
    public JMXCheckContext(Consumer<Throwable> onError)
    {
        this.onError = onError;
    }
    
    public void connect(String url, Map<String,?> env, Consumer<JMXConnection> onConnected)
    {
        try
        {
            JMXServiceURL jmxUrl = new JMXServiceURL(url);
            try (JMXConnector jmxc = JMXConnectorFactory.connect(jmxUrl, env))
            {
                onConnected.accept(new JMXConnection(jmxc.getMBeanServerConnection()));
            }
        }
        catch (Throwable t)
        {
            this.onError.accept(t);
        }
    }
    
    public void connect(String url, String username, String password, Consumer<JMXConnection> onConnected)
    {
        this.connect(url, buildEnv(username, password), onConnected);
    }
    
    public void connect(String host, int port, Map<String, ?> env, Consumer<JMXConnection> onConnected)
    {
        this.connect("service:jmx:rmi:///jndi/rmi://"+ host + ":" + port + "/jmxrmi", env, onConnected);
    }
    
    public void connect(String host, int port, Consumer<JMXConnection> onConnected)
    {
        this.connect(host, port, null, onConnected);
    }
    
    public void connect(String host, int port, String username, String password, Consumer<JMXConnection> onConnected)
    {
        
        this.connect(host, port, buildEnv(username, password), onConnected);
    }
    
    public Map<String, Object> buildEnv(String username, String password)
    {
        Map<String, Object> env = this.buildEnv();
        if (! Util.isEmpty(username))
        {
            env.put(JMXConnector.CREDENTIALS, new String[] { username, password });
        }
        return env;
    }
    
    public Map<String, Object> buildEnv()
    {
        Map<String, Object> env = new HashMap<>();
        return env;
    }
}
