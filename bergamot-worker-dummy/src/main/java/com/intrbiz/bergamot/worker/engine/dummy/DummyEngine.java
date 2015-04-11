package com.intrbiz.bergamot.worker.engine.dummy;

import com.intrbiz.bergamot.worker.engine.AbstractEngine;

public class DummyEngine extends AbstractEngine
{
    public static final String NAME = "dummy";
    
    public DummyEngine()
    {
        super(NAME);
    }
    
    @Override
    protected void configure() throws Exception
    {
        super.configure();
        // setup executors
        if (this.executors.isEmpty())
        {
            this.addExecutor(new StaticExecutor());
            this.addExecutor(new RandomExecutor());
            this.addExecutor(new TimedExecutor());
        }
    }
}
