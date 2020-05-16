package com.intrbiz.bergamot.worker.engine;

import java.util.Objects;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;

public abstract class AbstractCheckExecutor<T extends CheckEngine> implements CheckExecutor<T>
{
    protected final String name;
    
    protected final boolean defaultExecutor;
    
    protected T engine;
    
    protected CheckEngineContext engineContext;

    public AbstractCheckExecutor(final String name, final boolean defaultExecutor)
    {
        super();
        this.name = Objects.requireNonNull(name);
        this.defaultExecutor = defaultExecutor;
    }
    
    public AbstractCheckExecutor(final String name)
    {
        this(name, false);
    }
    
    @Override
    public final String getName()
    {
        return this.name;
    }

    protected final T getEngine()
    {
        return this.engine;
    }
    
    protected final CheckEngineContext getEngineContext()
    {
        return engineContext;
    }

    @Override
    public final void prepare(T engine, CheckEngineContext engineContext)
    {
        this.engine = engine;
        this.engineContext = engineContext;
        this.doPrepare(engine, engineContext);
    }
    
    protected void doPrepare(T engine, CheckEngineContext engineContext)
    {
    }
    
    @Override
    public void start(T engine, CheckEngineContext context)
    {
    }
    
    @Override
    public boolean accept(ExecuteCheck check)
    {
        return this.name.equalsIgnoreCase(check.getExecutor()) || (this.defaultExecutor && Util.isEmpty(check.getExecutor()));
    }
}
