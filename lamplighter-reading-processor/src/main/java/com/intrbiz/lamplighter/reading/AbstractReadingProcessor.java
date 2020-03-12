package com.intrbiz.lamplighter.reading;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.queue.ProcessingPoolConsumer;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;

public abstract class AbstractReadingProcessor implements ReadingProcessor
{
    private Logger logger = Logger.getLogger(AbstractReadingProcessor.class);
    
    protected final UUID poolId;
    
    private final ProcessingPoolConsumer consumer;

    private int threadCount;
    
    private Thread[] threads;
    
    protected AtomicBoolean run;
    
    protected AtomicBoolean started = new AtomicBoolean(false);
    
    protected CountDownLatch shutdownLatch;

    public AbstractReadingProcessor(UUID poolId, ProcessingPoolConsumer consumer)
    {
        super();
        this.poolId = Objects.requireNonNull(poolId);
        this.consumer = Objects.requireNonNull(consumer);
        this.threadCount = Runtime.getRuntime().availableProcessors();
    }
    
    public UUID getPoolId()
    {
        return this.poolId;
    }

    @Override
    public void start()
    {
        if (this.started.compareAndSet(false, true))
        {
            this.startExecutors();
        }
    }
    
    public void stop()
    {
        if (this.started.compareAndSet(true, false))
        {
            this.run.set(false);
            try
            {
                this.shutdownLatch.await();
            }
            catch (InterruptedException e)
            {
            }
        }
    }
    
    protected void startExecutors()
    {
        this.run = new AtomicBoolean(true);
        this.shutdownLatch = new CountDownLatch(this.threadCount);
        this.threads = new Thread[this.threadCount];
        for (int i = 0; i < this.threads.length; i++)
        {
            final int threadNum = i;
            this.threads[i] = new Thread(() -> {
                try
                {
                    logger.debug("Reading processor executor " + threadNum + " starting.");
                    while (this.run.get())
                    {
                        try
                        {
                            ReadingParcelMO reading = this.consumer.pollReading();
                            if (reading != null)
                            {
                                this.processReadings(reading);
                            }
                        }
                        catch (Exception e)
                        {
                            logger.error("Error processing result", e);
                        }
                    }
                    logger.debug("Reading processor executor " + threadNum + " stopped.");
                }
                finally
                {
                    this.shutdownLatch.countDown();
                }
            }, "Bergamot-Reading-Processor-Executor-" + i);
        }
    }
}
