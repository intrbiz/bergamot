package com.intrbiz.bergamot.worker.engine;

import com.intrbiz.configuration.Configuration;

/**
 * The context this engine is executing within
 */
public interface EngineContext
{
    Configuration getConfiguration();
}
