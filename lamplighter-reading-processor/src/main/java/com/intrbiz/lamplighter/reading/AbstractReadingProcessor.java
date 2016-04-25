package com.intrbiz.lamplighter.reading;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.bergamot.queue.key.ReadingKey;
import com.intrbiz.queue.Consumer;

public abstract class AbstractReadingProcessor implements ReadingProcessor
{
    private Logger logger = Logger.getLogger(AbstractReadingProcessor.class);
    
    private UUID instanceId = UUID.randomUUID();

    private WorkerQueue workerQueue;

    private List<Consumer<ReadingParcelMO, ReadingKey>> readingConsumers = new LinkedList<Consumer<ReadingParcelMO, ReadingKey>>();
    
    private List<Consumer<ReadingParcelMO, ReadingKey>> fallbackConsumers = new LinkedList<Consumer<ReadingParcelMO, ReadingKey>>();

    private int threads = Runtime.getRuntime().availableProcessors();

    public AbstractReadingProcessor()
    {
        super();
    }

    @Override
    public int getThreads()
    {
        return threads;
    }

    @Override
    public void setThreads(int threads)
    {
        this.threads = threads;
    }

    @Override
    public void ownPool(UUID site, int pool)
    {
        for (Consumer<ReadingParcelMO, ReadingKey> consumer : this.readingConsumers)
        {
            consumer.addBinding(new ReadingKey(site, pool));
            break;
        }
    }

    @Override
    public void disownPool(UUID site, int pool)
    {
        for (Consumer<ReadingParcelMO, ReadingKey> consumer : this.readingConsumers)
        {
            consumer.removeBinding(new ReadingKey(site, pool));
            break;
        }
    }

    @Override
    public void start()
    {
        // setup the consumer
        logger.info("Creating readings consumer");
        this.workerQueue = WorkerQueue.open();
        // create the consumers
        for (int i = 0; i < this.getThreads(); i++)
        {
            // consume results, currently for all sites
            this.readingConsumers.add(this.workerQueue.consumeReadings((h, r) -> {
                logger.trace("Processing pooled/site readings");
                processReadings(r);
            }, this.instanceId.toString()));
            // consume results, currently for all sites
            this.fallbackConsumers.add(this.workerQueue.consumeFallbackReadings((h, r) -> {
                logger.debug("Processing fallback readings");
                processReadings(r);
            }));
        }
    }
}
