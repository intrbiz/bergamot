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
    private static final Logger logger = Logger.getLogger(JDBCChecker.class);
    
    private static String[] DEFAULT_DRIVERS = {
            "org.postgresql.Driver",
            "com.mysql.cj.jdbc.Driver"
    };
    
    private final Timer timer;

    public JDBCChecker()
    {
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
        // register some common drivers
        try
        {
            for (String driver : DEFAULT_DRIVERS)
            {
                Class.forName(driver);
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to register default JDBC drivers", e);
        }
    }
    
    Timer getTimer()
    {
        return this.timer;
    }

    public JDBCCheckContext createContext(long timeout)
    {
        return new JDBCCheckContext(this, (t) -> { throw new JDBCException(t); }, timeout);
    }
    
    public JDBCCheckContext createContext(Consumer<Throwable> errorHandler, long timeout)
    {
        return new JDBCCheckContext(this, errorHandler, timeout);
    }

    public void shutdown()
    {
    }
}
