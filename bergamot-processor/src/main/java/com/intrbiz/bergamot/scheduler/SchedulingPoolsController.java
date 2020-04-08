package com.intrbiz.bergamot.scheduler;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.consumer.SchedulingPoolConsumer;
import com.intrbiz.bergamot.cluster.election.SchedulingPoolElector;
import com.intrbiz.bergamot.cluster.election.model.ElectionState;

/**
 * Controller for running scheduling pools
 */
public class SchedulingPoolsController
{
    private static final Logger logger = Logger.getLogger(SchedulingPoolsController.class);
    
    private final SchedulingPoolElector[] poolElectors;
    
    private final Scheduler scheduler;
    
    private final Function<Integer, SchedulingPoolConsumer> schedulingPoolConsumerFactory;
    
    private final ConcurrentMap<Integer, SchedulingPoolConsumer> pools = new ConcurrentHashMap<>();
    
    public SchedulingPoolsController(SchedulingPoolElector[] poolElectors, Scheduler scheduler, Function<Integer, SchedulingPoolConsumer> schedulingPoolConsumerFactory)
    {
        super();
        this.poolElectors = Objects.requireNonNull(poolElectors);
        this.scheduler = Objects.requireNonNull(scheduler);
        this.schedulingPoolConsumerFactory = Objects.requireNonNull(schedulingPoolConsumerFactory);
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
        synchronized (this)
        {
            if (! this.pools.containsKey(pool))
            {
                logger.info("Starting processing pool " + pool + ", now running " + this.pools.size() + " processing pools");
                // Schedule the pool
                this.scheduler.schedulePool(pool);
                // Register message consumer
                SchedulingPoolConsumer consumer = this.schedulingPoolConsumerFactory.apply(pool);
                this.pools.put(pool, consumer);
                consumer.listen(this.scheduler::process);
            }
        }
    }
    
    protected void stopPool(int pool)
    {
        synchronized (this)
        {
            // Stop the processing pool execution unit
            SchedulingPoolConsumer consumer = this.pools.remove(pool);
            if (consumer != null)
            {
                logger.info("Stopping scheduling pool " + pool + ", now running " + this.pools.size() + " processing pools");
                // Stop scheduler messages
                consumer.unlistenAll();
                // Unschedule the pool
                this.scheduler.unschedulePool(pool);
            }
        }
    }
    
    public void stop()
    {
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
