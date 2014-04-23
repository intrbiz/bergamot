package com.intrbiz.bergamot.worker.engine;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.model.message.task.Task;
import com.intrbiz.bergamot.worker.Engine;
import com.intrbiz.bergamot.worker.Executor;
import com.intrbiz.bergamot.worker.Worker;

public class AbstractEngine implements Engine
{
    private Logger logger = Logger.getLogger(AbstractEngine.class);

    protected Worker worker;

    protected final String name;
    
    protected EngineCfg config;

    protected List<Executor<?>> executors = new LinkedList<Executor<?>>();

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
        ((Executor)executor).setEngine(this);
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
    public void execute(Task task)
    {
        for (Executor<?> executor : this.executors)
        {
            if (executor.accept(task))
            {
                executor.execute(task);
                return;
            }
        }
        logger.warn("Failed to execute task " + task + ", no matching executor found, droping");
    }
}
