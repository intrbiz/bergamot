package com.intrbiz.bergamot.leader;

import java.util.UUID;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.consumer.ProcessorConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistry;
import com.intrbiz.bergamot.cluster.registry.model.RegistryEvent.Type;

public class ProcessorCleanup
{
    private static final Logger logger = Logger.getLogger(ProcessorCleanup.class);
    
    private final ProcessorRegistry processorRegistry;
    
    private final Function<UUID, ProcessorConsumer> processorConsumerFactory;
    
    private final ProcessorDispatcher processorDispatcher;
    
    private UUID listenerId;

    public ProcessorCleanup(ProcessorRegistry processorRegistry, Function<UUID, ProcessorConsumer> processorConsumerFactory, ProcessorDispatcher processorDispatcher)
    {
        super();
        this.processorRegistry = processorRegistry;
        this.processorConsumerFactory = processorConsumerFactory;
        this.processorDispatcher = processorDispatcher;
    }
    
    public void start()
    {
        this.listenerId = this.processorRegistry.listen((event) -> {
            if (event.getType() == Type.REMOVED)
            {
                this.cleanUpWorker(event.getId());
            }
        });
    }
    
    protected void cleanUpWorker(UUID worker)
    {
        logger.info("Cleaning up worker queue " + worker);
        ProcessorConsumer consumer = this.processorConsumerFactory.apply(worker);
        // Drain down the worker queue
        consumer.drainTo(this.processorDispatcher::dispatch);
        // Destroy the worker queue
        consumer.destroy();
    }
    
    public void stop()
    {
        if (this.listenerId != null)
        {
            this.processorRegistry.unlisten(this.listenerId);
            this.listenerId = null;
        }
    }
}
