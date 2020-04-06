package com.intrbiz.bergamot.pool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.AgentRegistrationService;
import com.intrbiz.bergamot.cluster.consumer.PoolConsumer;
import com.intrbiz.bergamot.model.message.pool.PoolMessage;
import com.intrbiz.bergamot.model.message.pool.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.pool.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.pool.result.ResultMessage;
import com.intrbiz.bergamot.model.message.pool.scheduler.SchedulerMessage;
import com.intrbiz.bergamot.result.ResultProcessor;
import com.intrbiz.bergamot.scheduler.Scheduler;
import com.intrbiz.lamplighter.reading.ReadingProcessor;

public class ProcessingPool
{
    private static final Logger logger = Logger.getLogger(ProcessingPool.class);
    
    private final int pool;
    
    private final PoolConsumer poolConsumer;
    
    private final Scheduler scheduler;
    
    private final ResultProcessor resultProcessor;
    
    private final ReadingProcessor readingProcessor;
    
    private final AgentRegistrationService agentRegistrationService;
    
    private final AtomicBoolean run = new AtomicBoolean(false);
    
    private Thread runner;
    
    private CountDownLatch shutdownLatch;
    
    public ProcessingPool(int pool, PoolConsumer poolConsumer, Scheduler scheduler, ResultProcessor resultProcessor, ReadingProcessor readingProcessor, AgentRegistrationService agentRegistrationService)
    {
        super();
        this.pool = pool;
        this.poolConsumer = poolConsumer;
        this.scheduler = scheduler;
        this.resultProcessor = resultProcessor;
        this.readingProcessor = readingProcessor;
        this.agentRegistrationService = agentRegistrationService;
    }
    
    public int getPool()
    {
        return this.pool;
    }
    
    public void start()
    {
        logger.info("Processing pool " + this.pool + " starting");
        if (this.run.compareAndSet(false, true))
        {
            // Create our execution thread
            this.shutdownLatch = new CountDownLatch(1);
            this.runner = new Thread(this::run, "bergamot-processing-pool" + this.pool);
            this.runner.start();
        }
    }
    
    protected void run()
    {
        logger.debug("Procesing pool " + this.pool + " thread starting");
        try
        {
            // Schedule this pool
            logger.info("Scheduling processing pool " + this.pool);
            this.scheduler.schedulePool(this.pool);
            // Start the run loop
            logger.info("Entering processing pool run loop");
            try
            {
                while (this.run.get())
                {
                    try
                    {
                        PoolMessage message = this.poolConsumer.poll(5, TimeUnit.SECONDS);
                        if (message != null)
                        {
                            if (message instanceof ResultMessage)
                            {
                                this.resultProcessor.process((ResultMessage) message);
                            }
                            else if (message instanceof ReadingParcelMO)
                            {
                                this.readingProcessor.process((ReadingParcelMO) message);
                            }
                            else if (message instanceof SchedulerMessage)
                            {
                                this.scheduler.process((SchedulerMessage) message);
                            }
                            else if (message instanceof AgentMessage)
                            {
                                this.agentRegistrationService.process((AgentMessage) message);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        logger.warn("Error processing pool message", e);
                    }
                }
            }
            finally
            {
                // Unschedule this pool
                logger.info("Unscheduling processing pool " + this.pool);
                this.scheduler.unschedulePool(this.pool);
            }
        }
        finally
        {
            this.shutdownLatch.countDown();
        }
        logger.debug("Procesing pool " + this.pool + " thread exited");
    }
    
    public void shutdown()
    {
        if (this.run.compareAndSet(true, false))
        {
            try
            {
                this.shutdownLatch.await();
            }
            catch (InterruptedException e)
            {
            }
            this.runner = null;
            this.shutdownLatch = null;
            logger.info("Processing pool " + this.pool + " shutdown");
        }
    }
}
