package com.intrbiz.bergamot.leader;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.election.LeaderElector;
import com.intrbiz.bergamot.cluster.election.PoolElector;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistry;

public class BergamotClusterLeader
{
    private static final Logger logger = Logger.getLogger(BergamotClusterLeader.class);
    
    private final LeaderElector leaderElector;
    
    private final ProcessingPoolBalancer processingPoolBalancer;
    
    private final AtomicBoolean run = new AtomicBoolean(false);
    
    private Thread runner;
    
    private final Object waitLock = new Object();
    
    private volatile long lastBalanceRun;
    
    private UUID processorListener;
    
    public BergamotClusterLeader(PoolElector[] poolElectors, ProcessorRegistry processorRegistry, LeaderElector leaderElector)
    {
        super();
        this.leaderElector = leaderElector;
        this.processingPoolBalancer = new ProcessingPoolBalancer(poolElectors, processorRegistry);
    }
    

    public void start()
    {
        if (this.run.compareAndSet(false, true))
        {
            this.runner = new Thread(this::run, "bergamot-cluster-leader");
            this.runner.setDaemon(true);
            this.runner.start();
        }
    }
    
    protected void run()
    {
        this.setup();
        try
        {
            while (this.run.get())
            {
                this.pause();
                this.runBalancer();
            }
        }
        finally
        {
            this.teardown();
        }
    }
    
    protected void setup()
    {
        // Setup listeners before starting the main run loop
        this.processorListener = this.leaderElector.listen((event) -> {
            logger.info("Detected new processor has joined cluster, triggering pool balance");
            this.lastBalanceRun = 0;
            synchronized (this.waitLock)
            {
                this.waitLock.notify();
            }
        });
    }
    
    protected void teardown()
    {
        this.leaderElector.unlisten(this.processorListener);
    }
    
    protected void runBalancer()
    {
        // Should we run the pool balancer
        if (this.run.get())
        {
            long age = System.nanoTime() - this.lastBalanceRun;
            if (age > 90_000_000_000L || age < 0)
            {
                this.processingPoolBalancer.balance();
                this.lastBalanceRun = System.nanoTime();
            }
        }
    }
    
    protected void pause()
    {
        // We don't need to be busy all the time
        if (this.run.get())
        {
            try
            {
                synchronized (this.waitLock)
                {
                    this.waitLock.wait(15_000L);
                }
            }
            catch (InterruptedException e)
            {
            }
        }
    }
    
    public void halt()
    {
        this.run.set(false);
        this.runner = null;
    }
    
}
