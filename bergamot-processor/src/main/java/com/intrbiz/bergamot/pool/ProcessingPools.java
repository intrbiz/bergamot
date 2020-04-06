package com.intrbiz.bergamot.pool;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.AgentRegistrationService;
import com.intrbiz.bergamot.cluster.consumer.PoolConsumer;
import com.intrbiz.bergamot.cluster.election.PoolElector;
import com.intrbiz.bergamot.cluster.election.model.ElectionState;
import com.intrbiz.bergamot.result.ResultProcessor;
import com.intrbiz.bergamot.scheduler.Scheduler;
import com.intrbiz.lamplighter.reading.ReadingProcessor;

public class ProcessingPools
{
    private static final Logger logger = Logger.getLogger(ProcessingPools.class);
    
    private final PoolElector[] poolElectors;
    
    private final Function<Integer, PoolConsumer> poolConsumerFactory;
    
    private final Scheduler scheduler;
    
    private final ResultProcessor resultProcessor;
    
    private final ReadingProcessor readingProcessor;
    
    private final AgentRegistrationService agentRegistrationService;
    
    private final ConcurrentMap<Integer, ProcessingPool> pools = new ConcurrentHashMap<>();
    
    public ProcessingPools(PoolElector[] poolElectors, Function<Integer, PoolConsumer> poolConsumerFactory, Scheduler scheduler, ResultProcessor resultProcessor, ReadingProcessor readingProcessor, AgentRegistrationService agentRegistrationService)
    {
        super();
        this.poolElectors = Objects.requireNonNull(poolElectors);
        this.poolConsumerFactory = Objects.requireNonNull(poolConsumerFactory);
        this.scheduler = Objects.requireNonNull(scheduler);
        this.resultProcessor = Objects.requireNonNull(resultProcessor);
        this.readingProcessor = Objects.requireNonNull(readingProcessor);
        this.agentRegistrationService = Objects.requireNonNull(agentRegistrationService);
    }
    
    public Collection<ProcessingPool> getPools()
    {
        return Collections.unmodifiableCollection(this.pools.values());
    }
    
    public void start() throws Exception
    {
        // Start our scheduler
        this.scheduler.start();
        // Join the election for each pool
        logger.info("Starting processing pool election");
        SecureRandom random = new SecureRandom();
        for (PoolElector pool : this.shufflePools())
        {
            logger.info("Electing pool " + pool.getPool());
            pool.elect((state) -> this.poolTrigger(pool.getPool(), state));
            Thread.sleep(random.nextInt(250));
        }
        logger.info("Finished processing pool election");
        logger.info("Started " + this.pools.size() + " processing pools");
    }
    
    protected List<PoolElector> shufflePools()
    {
        List<PoolElector> pools = new LinkedList<>(Arrays.asList(this.poolElectors));
        Collections.shuffle(pools);
        return pools;
    }
    
    protected void poolTrigger(int pool, ElectionState state)
    {
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
                logger.info("Starting processing pool " + pool);
                // Start the processing pool execution unit
                ProcessingPool processingPool = new ProcessingPool(pool, this.poolConsumerFactory.apply(pool), this.scheduler, this.resultProcessor, this.readingProcessor, this.agentRegistrationService);
                this.pools.put(pool, processingPool);
                processingPool.start();
            }
        }
    }
    
    protected void stopPool(int pool)
    {
        synchronized (this)
        {
            // Stop the processing pool execution unit
            ProcessingPool processingPool = this.pools.remove(pool);
            if (processingPool != null)
            {
                logger.info("Stopping processing pool " + pool);
                // Shutdown the pool
                processingPool.shutdown();
            }
        }
    }
    
    public void stop()
    {
        // Stop the scheduler
        this.scheduler.shutdown();
        // Stop all pools
        for (Entry<Integer, ProcessingPool> pool : this.pools.entrySet())
        {
            pool.getValue().shutdown();
            this.scheduler.unschedulePool(pool.getKey());
        }
        this.pools.clear();
        // Release all pools
        for (PoolElector pool : this.poolElectors)
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
