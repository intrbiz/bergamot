package com.intrbiz.bergamot.worker.runner;

import com.intrbiz.bergamot.config.RunnerCfg;
import com.intrbiz.bergamot.worker.Runner;
import com.intrbiz.bergamot.worker.Worker;

public abstract class AbstractRunner implements Runner
{
    protected Worker worker;
    
    protected RunnerCfg config;
    
    public AbstractRunner()
    {
        super();
    }

    @Override
    public void configure(RunnerCfg config) throws Exception
    {
        this.config = config;
        this.configure();
    }

    @Override
    public RunnerCfg getConfiguration()
    {
        return this.config;
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
    
    protected void configure() throws Exception
    {
    }
}
