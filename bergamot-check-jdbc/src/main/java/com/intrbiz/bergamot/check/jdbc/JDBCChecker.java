package com.intrbiz.bergamot.check.jdbc;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

/**
 * A JDBC based check framework
 */
public class JDBCChecker
{   
    private final Logger logger = Logger.getLogger(JDBCChecker.class);

    private int defaultRequestTimeoutSeconds;

    private int defaultConnectTimeoutSeconds;
    
    private final Timer timer;

    public JDBCChecker(int defaultConnectTimeoutSeconds, int defaultRequestTimeoutSeconds)
    {
        this.defaultRequestTimeoutSeconds = defaultRequestTimeoutSeconds;
        this.defaultConnectTimeoutSeconds = defaultConnectTimeoutSeconds;
        // timer
        this.timer = new Timer();
        // some util timer tasks
        // every 5 minutes forcefully purge the timer queue
        // we cancel most task (hopefully)
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run()
            {
                // purge the timer queue
                JDBCChecker.this.timer.purge();
            }
        }, 300_000L, 300_000L);
    }

    public JDBCChecker()
    {
        this(5, 60);
    }

    public int getDefaultRequestTimeoutSeconds()
    {
        return defaultRequestTimeoutSeconds;
    }

    public int getDefaultConnectTimeoutSeconds()
    {
        return defaultConnectTimeoutSeconds;
    }

    public JDBCCheckContext createContext()
    {
        return null;
    }
    
    public JDBCCheckContext createContext(Consumer<Exception> errorHandler)
    {
        return null;
    }

    public void shutdown()
    {
    }
}
