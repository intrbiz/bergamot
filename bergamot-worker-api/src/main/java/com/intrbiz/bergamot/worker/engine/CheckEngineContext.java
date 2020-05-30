package com.intrbiz.bergamot.worker.engine;

import java.util.UUID;
import java.util.function.Consumer;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;
import com.intrbiz.bergamot.model.message.processor.agent.ProcessorAgentMessage;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;

/**
 * The context this engine is executing within
 */
public interface CheckEngineContext
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
    void publishAgentAction(ProcessorAgentMessage action);
    
    /**
     * Register this worker as being the route for the given agent id.
     * 
     * The nonce should be random (UUID.randomUUID()) and needs to be tracked 
     * by the caller so that it can be provided to unregisterAgent.
     * 
     * @param agentId the agent id to register to this worker
     * @param nonce a random number used once (NONCE) which is used to validate unregistration
     */
    void registerAgent(UUID siteId, UUID agentId, UUID nonce);
    
    /**
     * Unregister this worker as being the route for the given agent id
     * 
     * @param agentId the agent id to unregister from this worker
     * @param nonce the random NONCE which was used when the agent was registered
     */
    void unregisterAgent(UUID siteId, UUID agentId, UUID nonce);
    
    /**
     * Publish a (probably passive) result
     */
    void publishResult(ResultMessage result);
    
    /**
     * Publish a set of readings
     */
    void publishReading(ReadingParcelMessage reading);
}
