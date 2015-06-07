package com.intrbiz.bergamot.worker.engine;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.bergamot.queue.key.ActiveResultKey;
import com.intrbiz.bergamot.queue.key.AgentBinding;
import com.intrbiz.bergamot.queue.key.ReadingKey;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.bergamot.queue.key.WorkerKey;
import com.intrbiz.bergamot.worker.Worker;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.RoutedProducer;

public class AbstractEngine implements Engine, DeliveryHandler<ExecuteCheck>
{
    private Logger logger = Logger.getLogger(AbstractEngine.class);

    protected Worker worker;

    protected final String name;

    protected EngineCfg config;

    protected List<Executor<?>> executors = new LinkedList<Executor<?>>();

    private WorkerQueue queue;

    private List<Consumer<ExecuteCheck, WorkerKey>> consumers = new LinkedList<Consumer<ExecuteCheck, WorkerKey>>();
    
    protected RoutedProducer<ResultMO, ResultKey> resultProducer;
    
    protected RoutedProducer<ReadingParcelMO, ReadingKey> readingProducer;

    public AbstractEngine(final String name)
    {
        super();
        this.name = name;
    }

    @Override
    public void configure(EngineCfg cfg) throws Exception
    {
        this.config = cfg;
        this.configure();
    }

    @Override
    public EngineCfg getConfiguration()
    {
        return this.config;
    }
    
    @Override
    public boolean isAgentRouted()
    {
        return false;
    }

    protected void configure() throws Exception
    {
        for (ExecutorCfg executorCfg : this.config.getExecutors())
        {
            Executor<?> executor = (Executor<?>) executorCfg.create();
            this.addExecutor(executor);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void addExecutor(Executor<?> executor)
    {
        ((Executor) executor).setEngine(this);
        this.executors.add(executor);
    }

    @Override
    public Collection<Executor<?>> getExecutors()
    {
        return this.executors;
    }

    @Override
    public Worker getWorker()
    {
        return this.worker;
    }

    @Override
    public void setWorker(Worker worker)
    {
        this.worker = worker;
    }

    @Override
    public String getName()
    {
        return this.name;
    }
    
    @Override
    public void publishResult(ResultKey key, ResultMO resultMO)
    {
        if (logger.isTraceEnabled())
        {
            logger.trace("Publishing result: " + resultMO.getId() + " " + resultMO.isOk() + " " + resultMO.getStatus() + " " + resultMO.getOutput());
        }
        this.resultProducer.publish(key, resultMO);
    }
    
    @Override
    public void publishReading(ReadingKey key, ReadingParcelMO readingParcelMO)
    {
        if (logger.isTraceEnabled())
        {
            logger.trace("Publishing reading: " + readingParcelMO.getReadings().size() + " " + readingParcelMO.getMatchOn());
        }
        this.readingProducer.publish(key, readingParcelMO);
    }
    
    @Override
    public void execute(ExecuteCheck task)
    {
        this.execute(task, (result) -> {
            this.publishResult(new ActiveResultKey(task.getSiteId(), task.getProcessingPool()), result);
        });
    }
    
    @Override
    public void execute(ExecuteCheck task, java.util.function.Consumer<ResultMO> onResult)
    {
        for (Executor<?> executor : this.executors)
        {
            if (executor.accept(task))
            {
                executor.execute(task, onResult);
                return;
            }
        }
        onResult.accept(new ActiveResultMO().fromCheck(task).error("No executor found to execute check"));
    }

    @Override
    public void start() throws Exception
    {
        // open the queue
        this.queue = WorkerQueue.open();
        // the result producer
        this.resultProducer = this.queue.publishResults();
        // the reading producer
        this.readingProducer = this.queue.publishReadings();
        // start the executors
        for (Executor<?> ex : this.getExecutors())
        {
            ex.start();
        }
        // start all the consumers
        for (int i = 0; i < this.getWorker().getConfiguration().getThreads(); i ++)
        {
            logger.trace("Creating consumer " + i);
            this.consumers.add(this.queue.consumeChecks(this, this.getWorker().getSite(), this.worker.getWorkerPool(), this.getName(), this.isAgentRouted(), this.getWorker().getId()));
        }
    }
    
    @Override
    public void bindAgent(UUID agentId)
    {
        logger.trace("Binding agent " + agentId + " to worker " + this.getWorker().getId());
        try
        {
            for (Consumer<ExecuteCheck, WorkerKey> consumer : this.consumers)
            {
                consumer.addBinding(new AgentBinding(agentId));
                break; // shared queue so only need to update bindings once
            }
        }
        catch (QueueException e)
        {
            logger.debug("Error binding agent", e);
        }
    }
    
    @Override
    public void unbindAgent(UUID agentId)
    {
        logger.trace("Unbinding agent " + agentId + " to worker " + this.getWorker().getId());
        try
        {
            for (Consumer<ExecuteCheck, WorkerKey> consumer : this.consumers)
            {
                consumer.removeBinding(new AgentBinding(agentId));
                break; // shared queue so only need to update bindings once
            }
        }
        catch (QueueException e)
        {
            logger.debug("Error binding agent", e);
        }
    }

    @Override
    public void handleDevliery(ExecuteCheck event) throws IOException
    {
        if (logger.isTraceEnabled())
            logger.trace("Got task: " + event);
        this.execute(event);
    }
}
