package com.intrbiz.bergamot.worker.engine.dummy;

import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.worker.engine.AbstractCheckEngine;

public class DummyEngine extends AbstractCheckEngine
{
    public static final String NAME = "dummy";
    
    public DummyEngine()
    {
        super(BergamotVersion.NAME, NAME, true,
                new StaticExecutor(),
                new RandomExecutor(),
                new TimedExecutor(),
                new ScriptExecutor());
    }
}
