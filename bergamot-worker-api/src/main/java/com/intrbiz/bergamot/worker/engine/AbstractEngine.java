package com.intrbiz.bergamot.worker.engine;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.bergamot.worker.Worker;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.RoutedProducer;

public class AbstractEngine implements Engine, DeliveryHandler<ExecuteCheck>
{
    private Logger logger = Logger.getLogger(AbstractEngine.class);

    protected Worker worker;

    protected final String name;

    protected EngineCfg config;

    protected List<Executor<?>> executors = new LinkedList<Executor<?>>();

    private WorkerQueue queue;

    private List<Consumer<ExecuteCheck>> consumers = new LinkedList<Consumer<ExecuteCheck>>();
    
    protected RoutedProducer<Result> resultProducer;

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
    public void execute(ExecuteCheck task)
    {
        this.execute(task, (result) -> {
            if (logger.isTraceEnabled()) 
                logger.trace("Publishing result: " + result.getId() + " " + result.isOk() + " " + result.getStatus() + " " + result.getOutput());
            this.resultProducer.publish(new ResultKey(task.getSiteId(), task.getProcessingPool()), result);
        });
    }
    
    @Override
    public void execute(ExecuteCheck task, java.util.function.Consumer<Result> onResult)
    {
        for (Executor<?> executor : this.executors)
        {
            if (executor.accept(task))
            {
                executor.execute(task, onResult);
                return;
            }
        }
        onResult.accept(new Result().fromCheck(task).error("No executor found to execute check"));
    }

    @Override
    public void start() throws Exception
    {
        // open the queue
        this.queue = WorkerQueue.open();
        // the producer
        this.resultProducer = this.queue.publishResults();
        // start the executors
        for (Executor<?> ex : this.getExecutors())
        {
            ex.start();
        }
        // start all the consumers
        for (int i = 0; i < this.getWorker().getConfiguration().getThreads(); i ++)
        {
            logger.trace("Creating consumer " + i);
            this.consumers.add(this.queue.consumeChecks(this, this.getWorker().getSite(), this.worker.getWorkerPool(), this.getName()));
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
