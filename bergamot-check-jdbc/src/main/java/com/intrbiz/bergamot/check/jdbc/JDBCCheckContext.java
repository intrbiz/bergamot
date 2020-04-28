package com.intrbiz.bergamot.check.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TimerTask;
import java.util.function.Consumer;

public class JDBCCheckContext
{   
    private final JDBCChecker checker;
    
    private final Consumer<Throwable> onError;
    
    private long defaultQueryTimeout = 30_000L;
    
    private long connectTimeout = 3;
    
    private boolean keepAlive = true;
    
    private Map<String, String> defaultProperties = new HashMap<>();
    
    private long hardTimeout = 60_000L;
    
    public JDBCCheckContext(JDBCChecker checker, Consumer<Throwable> onError, long timeout)
    {
        this.checker = checker;
        this.onError = onError;
        // default timeouts based on the base timeout
        this.defaultQueryTimeout = timeout / 2;
        this.hardTimeout = timeout;
    }
    
    JDBCChecker getChecker()
    {
        return this.checker;
    }
    
    public JDBCCheckContext driver(String driverName) throws Exception
    {
        Class.forName(driverName);
        return this;
    }
    
    public long connectTimeout(long connectTimeout)
    {
        this.connectTimeout = connectTimeout;
        return this.connectTimeout;
    }
    
    public long connectTimeout()
    {
        return this.connectTimeout;
    }
    
    public long defaultQueryTimeout(long defaultQueryTimeout)
    {
        this.defaultQueryTimeout = defaultQueryTimeout;
        return this.defaultQueryTimeout;
    }
    
    public long defaultQueryTimeout()
    {
        return this.defaultQueryTimeout;
    }
    
    public Map<String, String> defaultProperties()
    {
        return this.defaultProperties;
    }
    
    public String defaultProperty(String key, String value)
    {
        return (value == null) ? this.defaultProperties.remove(key) : this.defaultProperties.put(key, value);
    }
    
    public String defaultProperty(String key)
    {
        return this.defaultProperties.get(key);
    }
    
    public boolean keepAlive(boolean value)
    {
        this.keepAlive = value;
        return this.keepAlive;
    }
    
    public boolean keepAlive()
    {
        return this.keepAlive;
    }
    
    public long hardTimeout(long timeout)
    {
        this.hardTimeout = timeout;
        return this.hardTimeout;
    }
    
    public long hardTimeout()
    {
        return this.hardTimeout;
    }
    
    private void populateDefaultProperties(Properties props, String username, String password)
    {
        // auth properties
        props.setProperty("user", username);
        props.setProperty("password", password);
        // set know properties
        props.setProperty("connectTimeout", String.valueOf(this.connectTimeout));
        props.setProperty("loginTimeout", String.valueOf(this.connectTimeout));
        props.setProperty("socketTimeout", String.valueOf(this.defaultQueryTimeout / 1000L));
        props.setProperty("tcpKeepAlive", String.valueOf(this.keepAlive));
        // copy in the default properties
        for (Entry<String, String> entry : this.defaultProperties.entrySet())
        {
            props.setProperty(entry.getKey(), entry.getValue());
        }
    }
    
    public void connect(String url, String username, String password, Consumer<JDBCConnection> onConnected)
    {
        // setup the connection properties
        Properties props = new Properties();
        this.populateDefaultProperties(props, username, password);
        // connect
        try (Connection connection = DriverManager.getConnection(url, props))
        {
            TimerTask task = this.scheduleHardTimeout(connection);
            try
            {
                onConnected.accept(new JDBCConnection(this, connection));
            }
            finally
            {
                task.cancel();
            }
        }
        catch (Throwable t)
        {
            this.onError.accept(t);
        }
    }
    
    public void connect(String url, String username, String password, Consumer<JDBCConnection> onConnected, Consumer<Throwable> onError)
    {
        // setup the connection properties
        Properties props = new Properties();
        this.populateDefaultProperties(props, username, password);
        // connect
        try (Connection connection = DriverManager.getConnection(url, props))
        {
            TimerTask task = this.scheduleHardTimeout(connection);
            try
            {
                onConnected.accept(new JDBCConnection(this, connection));
            }
            finally
            {
                task.cancel();
            }
        }
        catch (Throwable t)
        {
            onError.accept(t);
        }
    }
    
    private TimerTask scheduleHardTimeout(Connection connection)
    {
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    if (connection != null && (! connection.isClosed()))
                    {
                        connection.close();
                    }
                }
                catch (Exception e)
                {
                }
            }
        };
        this.checker.getTimer().schedule(task, this.hardTimeout);
        return task;
    }
}
