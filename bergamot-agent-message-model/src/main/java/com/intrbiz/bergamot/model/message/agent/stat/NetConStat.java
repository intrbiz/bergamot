package com.intrbiz.bergamot.model.message.agent.stat;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.stat.netcon.NetConInfo;

@JsonTypeName("bergamot.agent.stat.netcon")
public class NetConStat extends AgentMessage
{    
    @JsonProperty("connections")
    private List<NetConInfo> connections = new LinkedList<NetConInfo>();

    public NetConStat()
    {
        super();
    }

    public NetConStat(AgentMessage message)
    {
        super(message);
    }

    public NetConStat(String id)
    {
        super(id);
    }

    public List<NetConInfo> getConnections()
    {
        return connections;
    }

    public void setConnections(List<NetConInfo> connections)
    {
        this.connections = connections;
    }
}
