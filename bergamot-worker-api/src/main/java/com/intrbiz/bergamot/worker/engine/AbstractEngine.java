package com.intrbiz.bergamot.worker.engine;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;

public abstract class AbstractEngine implements Engine
{
    protected final String name;

    protected final List<Executor<?>> executors;
    
    protected EngineContext engineContext;

    protected AbstractEngine(final String name, Executor<?>... executors)
    {
        super();
        this.name = Objects.requireNonNull(name);
        this.executors = Collections.unmodifiableList(Arrays.asList(Objects.requireNonNull(executors)));
    }
    
    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public Collection<Executor<?>> getExecutors()
    {
        return this.executors;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public final void prepare(EngineContext context) throws Exception
    {
        this.engineContext = context;
        this.doPrepare(context);
        for (Executor<?> ex : this.getExecutors())
        {
            ((Executor) ex).prepare(this, context);
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public final void start(EngineContext context) throws Exception
    {
        for (Executor<?> ex : this.getExecutors())
        {
            ((Executor) ex).start(this, context);
        }
        this.doStart(context);
    }
    
    @Override
    public void execute(ExecuteCheck check, CheckExecutionContext context)
    {
        Executor<?> executor = this.selectExecutor(check);
        if (executor != null)
        {
            executor.execute(check, context);
        }
        else
        {
            context.publishResult(new ActiveResult().fromCheck(check).error("No executor found to execute check"));
        }
    }
    
    @Override
    public boolean accept(ExecuteCheck task)
    {
        for (Executor<?> executor : this.executors)
        {
            if (executor.accept(task))
                return true;
        }
        return false;
    }

    protected Executor<?> selectExecutor(ExecuteCheck task)
    {
        for (Executor<?> executor : this.executors)
        {
            if (executor.accept(task))
                return executor;
        }
        return null;
    }
    
    public final void shutdown(EngineContext engineContext)
    {
        this.doShutdown(engineContext);
    }
    
    protected final EngineContext getEngineContext()
    {
        return this.engineContext;
    }
    
    protected void doPrepare(EngineContext engineContext) throws Exception
    {
    }
    
    protected void doStart(EngineContext engineContext) throws Exception
    {
    }
    
    protected void doShutdown(EngineContext engineContext)
    {
    }
}
