package com.intrbiz.bergamot.worker.engine.dummy;

import com.intrbiz.bergamot.worker.engine.AbstractEngine;

public class DummyEngine extends AbstractEngine
{
    public static final String NAME = "dummy";
    
    public DummyEngine()
    {
        super(NAME, 
                new StaticExecutor(),
                new RandomExecutor(),
                new TimedExecutor());
    }
}
