package com.intrbiz.bergamot.worker.engine;

public abstract class AbstractExecutor<T extends Engine> implements Executor<T>
{
    protected T engine;
    
    protected EngineContext engineContext;

    public AbstractExecutor()
    {
        super();
    }

    protected final T getEngine()
    {
        return this.engine;
    }
    
    protected final EngineContext getEngineContext()
    {
        return engineContext;
    }

    @Override
    public final void prepare(T engine, EngineContext engineContext)
    {
        this.engine = engine;
        this.engineContext = engineContext;
        this.doPrepare(engine, engineContext);
    }
    
    protected void doPrepare(T engine, EngineContext engineContext)
    {
    }
    
    @Override
    public void start(T engine, EngineContext context)
    {
    }
}
