package com.intrbiz.lamplighter.reading;

import java.util.Objects;
import java.util.UUID;

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
    
    protected volatile boolean run = false;

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
        this.startExecutors();
    }
    
    protected void startExecutors()
    {
        this.run = true;
        this.threads = new Thread[this.threadCount];
        for (int i = 0; i < this.threads.length; i++)
        {
            final int threadNum = i;
            this.threads[i] = new Thread(() -> {
                logger.debug("Reading processor executor " + threadNum + " starting.");
                while (this.run)
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
            }, "Bergamot-Reading-Processor-Executor-" + i);
        }
    }
}
