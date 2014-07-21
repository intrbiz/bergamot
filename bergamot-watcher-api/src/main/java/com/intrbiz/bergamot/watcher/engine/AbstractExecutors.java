package com.intrbiz.bergamot.watcher.engine;

import com.intrbiz.bergamot.config.ExecutorCfg;

public abstract class AbstractExecutors<T extends Engine> implements Executor<T>
{
    protected T Engine;

    protected ExecutorCfg config;

    public AbstractExecutors()
    {
        super();
    }

    @Override
    public T getEngine()
    {
        return this.Engine;
    }

    @Override
    public void setEngine(T engine)
    {
        this.Engine = engine;
    }

    @Override
    public void configure(ExecutorCfg config) throws Exception
    {
        this.config = config;
        this.configure();
    }

    @Override
    public ExecutorCfg getConfiguration()
    {
        return this.config;
    }

    protected void configure() throws Exception
    {
    }
    
    @Override
    public void start()
    {
    }
}
