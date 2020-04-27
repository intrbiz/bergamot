package com.intrbiz.bergamot.worker.engine;

import java.util.UUID;
import java.util.function.Consumer;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;
import com.intrbiz.bergamot.model.message.processor.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;

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
    void lookupAgentKey(UUID keyId, Consumer<AgentAuthenticationKey> callback);
    
    /**
     * The agent event queue for sending register events
     */
    void publishAgentAction(AgentMessage action);
    
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
    void publishResult(ResultMessage result);
    
    /**
     * Publish a set of readings
     */
    void publishReading(ReadingParcelMO reading);
}
