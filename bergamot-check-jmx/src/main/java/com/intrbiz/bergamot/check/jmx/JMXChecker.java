package com.intrbiz.bergamot.check.jmx;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * A JDBC based check framework
 */
public class JMXChecker
{   
    private int defaultTimeoutSeconds;
    
    private final Timer timer;

    public JMXChecker(int defaultTimeoutSeconds)
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
                JMXChecker.this.timer.purge();
            }
        }, 300_000L, 300_000L);
    }

    public JMXChecker()
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

    public JMXCheckContext createContext()
    {
        return new JMXCheckContext((t) -> { throw new JMXException(t); });
    }
    
    public JMXCheckContext createContext(Consumer<Throwable> errorHandler)
    {
        return new JMXCheckContext(errorHandler);
    }

    public void shutdown()
    {
    }
}
