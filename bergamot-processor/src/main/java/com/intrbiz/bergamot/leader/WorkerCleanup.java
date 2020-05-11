package com.intrbiz.bergamot.leader;

import java.util.UUID;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.WorkerDispatcher;
import com.intrbiz.bergamot.cluster.registry.WorkerRegistry;
import com.intrbiz.bergamot.cluster.registry.model.RegistryEvent.Type;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;

public class WorkerCleanup
{
    private static final Logger logger = Logger.getLogger(WorkerCleanup.class);
    
    private final WorkerRegistry workerRegistry;
    
    private final Function<UUID, WorkerConsumer> workerConsumerFactory;
    
    private final WorkerDispatcher checkDispatcher;
    
    private UUID listenerId;

    public WorkerCleanup(WorkerRegistry workerRegistry, Function<UUID, WorkerConsumer> workerConsumerFactory, WorkerDispatcher checkDispatcher)
    {
        super();
        this.workerRegistry = workerRegistry;
        this.workerConsumerFactory = workerConsumerFactory;
        this.checkDispatcher = checkDispatcher;
    }
    
    public void start()
    {
        this.listenerId = this.workerRegistry.listen((event) -> {
            if (event.getType() == Type.REMOVED)
            {
                this.cleanUpWorker(event.getId());
            }
        });
    }
    
    protected void cleanUpWorker(UUID worker)
    {
        logger.info("Cleaning up worker queue " + worker);
        WorkerConsumer consumer = this.workerConsumerFactory.apply(worker);
        // Drain down the worker queue
        consumer.drainTo((message) -> {
            // only bother resending check executions
            if (message instanceof ExecuteCheck)
            {
                // reset the worker id and dispatch
                message.setWorkerId(null);
                this.checkDispatcher.dispatch(message);
            }
        });
        // Destroy the worker queue
        consumer.destroy();
    }
    
    public void stop()
    {
        if (this.listenerId != null)
        {
            this.workerRegistry.unlisten(this.listenerId);
            this.listenerId = null;
        }
    }
}
