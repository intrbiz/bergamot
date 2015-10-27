package com.intrbiz.bergamot.check.jmx;

import java.util.function.Consumer;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JMXCheckContext
{    
    private final Consumer<Throwable> onError;
    
    public JMXCheckContext(Consumer<Throwable> onError)
    {
        this.onError = onError;
    }
    
    public void connect(String url, Consumer<JMXConnection> onConnected)
    {
        try
        {
            JMXServiceURL jmxUrl = new JMXServiceURL(url);
            try (JMXConnector jmxc = JMXConnectorFactory.connect(jmxUrl))
            {
                onConnected.accept(new JMXConnection(jmxc.getMBeanServerConnection()));
            }
        }
        catch (Throwable t)
        {
            this.onError.accept(t);
        }
    }
    
    public void connect(String host, int port, Consumer<JMXConnection> onConnected)
    {
        this.connect("service:jmx:rmi:///jndi/rmi://"+ host + ":" + port + "/jmxrmi", onConnected);
    }
}
