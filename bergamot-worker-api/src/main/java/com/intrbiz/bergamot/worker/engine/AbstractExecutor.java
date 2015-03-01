package com.intrbiz.bergamot.worker.engine;

import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.queue.key.ResultKey;

public abstract class AbstractExecutor<T extends Engine> implements Executor<T>
{
    protected T Engine;

    protected ExecutorCfg config;

    public AbstractExecutor()
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
    
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return task instanceof ExecuteCheck;
    }

    @Override
    public void publishResult(ResultKey key, Result result)
    {
        this.getEngine().publishResult(key, result);
    }
}
