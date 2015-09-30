package com.intrbiz.bergamot.check.jdbc;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * A JDBC based check framework
 */
public class JDBCChecker
{   
    private int defaultTimeoutSeconds;
    
    private final Timer timer;

    public JDBCChecker(int defaultTimeoutSeconds)
    {
        this.defaultTimeoutSeconds = defaultTimeoutSeconds;
        //
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
        this(60);
    }

    public int getTimeoutSeconds()
    {
        return defaultTimeoutSeconds;
    }
    
    Timer getTimer()
    {
        return this.timer;
    }

    public JDBCCheckContext createContext()
    {
        return new JDBCCheckContext((t) -> { throw new JDBCException(t); });
    }
    
    public JDBCCheckContext createContext(Consumer<Throwable> errorHandler)
    {
        return new JDBCCheckContext(errorHandler);
    }

    public void shutdown()
    {
    }
}
