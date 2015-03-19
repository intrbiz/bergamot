package com.intrbiz.bergamot.model.message.agent.hello;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.hello")
public class AgentHello extends AgentMessage
{
    @JsonProperty("service_id")
    private UUID serviceId;

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("agent_name")
    private String agentName;

    @JsonProperty("agent_variant")
    private String agentVariant;

    @JsonProperty("agent_version")
    private String agentVersion;
    
    @JsonProperty("protocol_version")
    private int protocolVersion;
    
    @JsonProperty("nonce")
    private String nonce;
    
    @JsonProperty("timestamp")
    private long timestamp;

    public AgentHello()
    {
        super();
    }

    public AgentHello(AgentMessage message)
    {
        super(message);
    }

    public AgentHello(String id)
    {
        super(id);
    }

    public UUID getServiceId()
    {
        return serviceId;
    }

    public void setServiceId(UUID serviceId)
    {
        this.serviceId = serviceId;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }

    public String getAgentName()
    {
        return agentName;
    }

    public void setAgentName(String agentName)
    {
        this.agentName = agentName;
    }

    public String getAgentVariant()
    {
        return agentVariant;
    }

    public void setAgentVariant(String agentVariant)
    {
        this.agentVariant = agentVariant;
    }

    public String getAgentVersion()
    {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion)
    {
        this.agentVersion = agentVersion;
    }

    public String getNonce()
    {
        return nonce;
    }

    public void setNonce(String nonce)
    {
        this.nonce = nonce;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public int getProtocolVersion()
    {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion)
    {
        this.protocolVersion = protocolVersion;
    }
}
