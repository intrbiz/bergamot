package com.intrbiz.bergamot.leader;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;
import com.intrbiz.bergamot.cluster.consumer.ProcessorConsumer;
import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.CheckDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.NotificationDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.election.LeaderElector;
import com.intrbiz.bergamot.cluster.election.SchedulingPoolElector;
import com.intrbiz.bergamot.cluster.election.model.ElectionState;
import com.intrbiz.bergamot.cluster.registry.NotifierRegistry;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistry;
import com.intrbiz.bergamot.cluster.registry.WorkerRegistry;

public class BergamotClusterLeader
{
    private static final Logger logger = Logger.getLogger(BergamotClusterLeader.class);
    
    private final LeaderElector leaderElector;
    
    private final ProcessingPoolBalancer processingPoolBalancer;
    
    private final WorkerCleanup workerCleanup;
    
    private final ProcessorCleanup processorCleanup;
    
    private final NotifierCleanup notifierCleanup;
    
    private final AtomicBoolean run = new AtomicBoolean(false);
    
    private Thread runner;
    
    private final Object waitLock = new Object();
    
    private volatile long lastBalanceRun;
    
    private UUID processorListener;
    
    public BergamotClusterLeader(
            SchedulingPoolElector[] poolElectors,
            LeaderElector leaderElector,
            ProcessorRegistry processorRegistry, 
            Function<UUID, ProcessorConsumer> processorConsumerFactory, 
            ProcessorDispatcher processorDispatcher,
            WorkerRegistry workerRegistry, 
            Function<UUID, WorkerConsumer> workerConsumerFactory, 
            CheckDispatcher checkDispatcher,
            NotifierRegistry notifierRegistry, 
            Function<UUID, NotificationConsumer> notificationConsumerFactory, 
            NotificationDispatcher notificationDispatcher
    ) {
        super();
        this.leaderElector = leaderElector;
        this.processingPoolBalancer = new ProcessingPoolBalancer(poolElectors, processorRegistry, leaderElector);
        this.processorCleanup = new ProcessorCleanup(processorRegistry, processorConsumerFactory, processorDispatcher);
        this.workerCleanup = new WorkerCleanup(workerRegistry, workerConsumerFactory, checkDispatcher);
        this.notifierCleanup = new NotifierCleanup(notifierRegistry, notificationConsumerFactory, notificationDispatcher);
    }
    
    public void start()
    {
        // start shared tasks
        this.processorCleanup.start();
        this.workerCleanup.start();
        this.notifierCleanup.start();
    }
    
    public void stop()
    {
        // Stop the leader thread
        this.haltLeaderOnlyTasks();
        // stop shared tasks
        this.processorCleanup.stop();
        this.workerCleanup.stop();
        this.notifierCleanup.stop();
    }
    
    protected void launchLeaderOnlyTasks()
    {
        if (this.run.compareAndSet(false, true))
        {
            this.runner = new Thread(this::run, "bergamot-cluster-leader");
            this.runner.setDaemon(true);
            this.runner.start();
        }
    }
    
    public void haltLeaderOnlyTasks()
    {
        this.run.set(false);
        synchronized (this.waitLock)
        {
            this.waitLock.notifyAll();
        }
        this.runner = null;
    }
    
    public void launch(ElectionState state)
    {
        if (state == ElectionState.LEADER)
        {
            logger.info("Starting cluster leader duties.");
            this.launchLeaderOnlyTasks();
        }
        else
        {
            logger.info("Halting cluster leader duties.");
            this.haltLeaderOnlyTasks();
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
                this.waitLock.notifyAll();
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
}
