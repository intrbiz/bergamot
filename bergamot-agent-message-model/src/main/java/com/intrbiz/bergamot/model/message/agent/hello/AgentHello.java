package com.intrbiz.bergamot.model.message.agent.hello;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.hello")
public class AgentHello extends AgentMessage
{
    @JsonProperty("host_id")
    private UUID hostId;

    @JsonProperty("host_name")
    private String hostName;

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

    public UUID getHostId()
    {
        return hostId;
    }

    public void setHostId(UUID hostId)
    {
        this.hostId = hostId;
    }

    public String getHostName()
    {
        return hostName;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
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
}
