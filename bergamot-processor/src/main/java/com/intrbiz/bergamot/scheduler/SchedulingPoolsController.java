package com.intrbiz.bergamot.scheduler;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.broker.SchedulingTopic;
import com.intrbiz.bergamot.cluster.election.SchedulingPoolElector;
import com.intrbiz.bergamot.cluster.election.model.ElectionState;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerMessage;

/**
 * Controller for running scheduling pools
 */
public class SchedulingPoolsController
{
    private static final Logger logger = Logger.getLogger(SchedulingPoolsController.class);
    
    private final SchedulingPoolElector[] poolElectors;
    
    private final Scheduler scheduler;
    
    private final SchedulingTopic schedulingTopic;
    
    private final ConcurrentMap<Integer, UUID> pools = new ConcurrentHashMap<>();
    
    private final AtomicBoolean[] runningPools;
    
    private UUID listenerId;
    
    public SchedulingPoolsController(SchedulingPoolElector[] poolElectors, Scheduler scheduler, SchedulingTopic schedulingTopic)
    {
        super();
        this.poolElectors = Objects.requireNonNull(poolElectors);
        this.scheduler = Objects.requireNonNull(scheduler);
        this.schedulingTopic = Objects.requireNonNull(schedulingTopic);
        // Init our internal state
        this.runningPools = new AtomicBoolean[this.poolElectors.length];
        for (int i = 0; i < this.runningPools.length; i++)
        {
            this.runningPools[i] = new AtomicBoolean(false);
        }
    }
    
    public Set<Integer> getPools()
    {
        return new TreeSet<>(this.pools.keySet());
    }
    
    public void start() throws Exception
    {
        // Start our scheduler
        this.scheduler.start();
        // Join the election for each pool
        logger.info("Starting processing pool election");
        for (SchedulingPoolElector pool : this.shufflePools())
        {
            logger.info("Electing pool " + pool.getPool());
            pool.elect((state) -> this.poolTrigger(pool.getPool(), state));
        }
        logger.info("Finished processing pool election");
        // Start listening to scheduler messages
        this.listenerId = this.schedulingTopic.listen(this::processSchedulingMessage);
        // All started
        logger.info("Started " + this.pools.size() + " processing pools");
    }
    
    protected List<SchedulingPoolElector> shufflePools()
    {
        List<SchedulingPoolElector> pools = new LinkedList<>(Arrays.asList(this.poolElectors));
        Collections.shuffle(pools);
        return pools;
    }
    
    protected void poolTrigger(int pool, ElectionState state)
    {
        logger.info("Got trigger for pool " + pool + " " + state);
        if (state == ElectionState.LEADER)
        {
            this.startPool(pool);
        }
        else
        {
            this.stopPool(pool);
        }
    }
    
    protected void startPool(int pool)
    {
        // Only start the scheduling pool if we don't already have it
        if (this.runningPools[pool].compareAndSet(false, true))
        {
            // Schedule the pool
            this.scheduler.schedulePool(pool);
            logger.info("Scheduling processing pool " + pool + ", now running " + this.pools.size() + " processing pools");
        }
    }
    
    protected void processSchedulingMessage(SchedulerMessage message)
    {
        logger.info("Got scheduling event for pool " + message.getPool() + ":\n" + message);
        // Only process the message if we are running the given pool
        if (this.runningPools[message.getPool()].get())
        {
            this.scheduler.process(message);
        }
    }
    
    protected void stopPool(int pool)
    {
        // Only stop the scheduling pool if we don't already have it
        if (this.runningPools[pool].compareAndSet(true, false))
        {
            // Unschedule the pool
            this.scheduler.unschedulePool(pool);
            logger.info("Unscheduling pool " + pool + ", now running " + this.pools.size() + " processing pools");
        }
    }
    
    public void stop()
    {
        // Stop listening to scheduler messages
        if (this.listenerId != null)
        {
            this.schedulingTopic.unlisten(this.listenerId);
        }
        // Stop the scheduler
        this.scheduler.shutdown();
        // Clear our pools map
        this.pools.clear();
        // Leave all pool elections
        for (SchedulingPoolElector pool : this.poolElectors)
        {
            try
            {
                pool.release();
            }
            catch (Exception e)
            {
                logger.info("Failed to release pool " + pool.getPool());
            }
        }
    }
}
