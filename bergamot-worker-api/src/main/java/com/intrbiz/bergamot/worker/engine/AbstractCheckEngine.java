package com.intrbiz.bergamot.worker.engine;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;

public abstract class AbstractCheckEngine implements CheckEngine
{
    protected final String name;
    
    protected final String vendor;
    
    protected final boolean enabledByDefault;

    protected final List<CheckExecutor<?>> executors;
    
    protected CheckEngineContext engineContext;

    protected AbstractCheckEngine(final String vendor, final String name, final boolean enabledByDefault, CheckExecutor<?>... executors)
    {
        super();
        this.vendor = Objects.requireNonNull(vendor);
        this.name = Objects.requireNonNull(name);
        this.enabledByDefault = enabledByDefault;
        this.executors = Collections.unmodifiableList(Arrays.asList(Objects.requireNonNull(executors)));
    }
    
    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String getVendor()
    {
        return this.vendor;
    }

    @Override
    public boolean isEnabledByDefault()
    {
        return this.enabledByDefault;
    }

    @Override
    public Collection<CheckExecutor<?>> getExecutors()
    {
        return this.executors;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public final void prepare(CheckEngineContext context) throws Exception
    {
        this.engineContext = context;
        this.doPrepare(context);
        for (CheckExecutor<?> ex : this.getExecutors())
        {
            ((CheckExecutor) ex).prepare(this, context);
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public final void start(CheckEngineContext context) throws Exception
    {
        for (CheckExecutor<?> ex : this.getExecutors())
        {
            ((CheckExecutor) ex).start(this, context);
        }
        this.doStart(context);
    }
    
    @Override
    public void execute(ExecuteCheck check, CheckExecutionContext context)
    {
        CheckExecutor<?> executor = this.selectExecutor(check);
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
        for (CheckExecutor<?> executor : this.executors)
        {
            if (executor.accept(task))
                return true;
        }
        return false;
    }

    protected CheckExecutor<?> selectExecutor(ExecuteCheck task)
    {
        for (CheckExecutor<?> executor : this.executors)
        {
            if (executor.accept(task))
                return executor;
        }
        return null;
    }
    
    public final void shutdown(CheckEngineContext engineContext)
    {
        this.doShutdown(engineContext);
    }
    
    protected final CheckEngineContext getEngineContext()
    {
        return this.engineContext;
    }
    
    protected void doPrepare(CheckEngineContext engineContext) throws Exception
    {
    }
    
    protected void doStart(CheckEngineContext engineContext) throws Exception
    {
    }
    
    protected void doShutdown(CheckEngineContext engineContext)
    {
    }
    
    public String toString()
    {
        return this.vendor + "::" + this.name;
    }
}
