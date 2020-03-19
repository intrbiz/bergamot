package com.intrbiz.bergamot.worker.engine;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.cluster.broker.AgentEventQueue;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyLookup;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;

/**
 * The context this engine is executing within
 */
public interface EngineContext
{
    /**
     * Get the worker configuration
     */
    default String getParameter(String name, String defaultValue)
    {
        return defaultValue;
    }
    
    default int getIntParameter(String name, int defaultValue)
    {
        String value = this.getParameter(name, null);
        return ! Util.isEmpty(value) ? Integer.parseInt(value) : defaultValue;
    }
    
    default long getLongParameter(String name, long defaultValue)
    {
        String value = this.getParameter(name, null);
        return ! Util.isEmpty(value) ? Long.parseLong(value) : defaultValue;
    }
    
    default float getFloatParameter(String name, float defaultValue)
    {
        String value = this.getParameter(name, null);
        return ! Util.isEmpty(value) ? Float.parseFloat(value) : defaultValue;
    }
    
    default double getDoubleParameter(String name, double defaultValue)
    {
        String value = this.getParameter(name, null);
        return ! Util.isEmpty(value) ? Double.parseDouble(value) : defaultValue;
    }
    
    default boolean getBooleanParameter(String name, boolean defaultValue)
    {
        String value = this.getParameter(name, null);
        return ! Util.isEmpty(value) ? Boolean.parseBoolean(value) : defaultValue;
    }
    
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
