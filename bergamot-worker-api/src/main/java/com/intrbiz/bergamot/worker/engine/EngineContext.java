package com.intrbiz.bergamot.worker.engine;

import java.util.UUID;

import com.intrbiz.bergamot.cluster.broker.AgentEventQueue;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyLookup;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.configuration.Configuration;

/**
 * The context this engine is executing within
 */
public interface EngineContext
{
    /**
     * Get the worker configuration
     */
    Configuration getConfiguration();
    
    /**
     * The agent key lookup service
     */
    AgentKeyLookup getAgentKeyLookup();
    
    /**
     * The agent event queue for sending register events
     */
    AgentEventQueue getAgentEventQueue();
    
    /**
     * Register this worker as being the route for the given agent id
     */
    void registerAgent(UUID agentId);
    
    /**
     * Unregister this worker as being the route for the given agent id
     */
    void unregisterAgent(UUID agentId);
    
    /**
     * Publish a (probably passive) result
     */
    void publishResult(ResultMO result);
    
    /**
     * Publish a set of readings
     */
    void publishReading(ReadingParcelMO readingParcelMO);
}
