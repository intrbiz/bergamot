package com.intrbiz.bergamot.check.ssh;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class SSHChecker
{
    private int defaultTimeoutSeconds;
    
    private final Timer timer;
    
    public SSHChecker(int defaultTimeoutSeconds)
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
                SSHChecker.this.timer.purge();
            }
        }, 300_000L, 300_000L);
    }

    public SSHChecker()
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

    public SSHCheckContext createContext()
    {
        return new SSHCheckContext((t) -> { throw new SSHException(t); });
    }
    
    public SSHCheckContext createContext(Consumer<Throwable> errorHandler)
    {
        return new SSHCheckContext(errorHandler);
    }

    public void shutdown()
    {
    }
}
