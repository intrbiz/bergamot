package com.intrbiz.bergamot.model.message.agent.check;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.check.netio")
public class CheckNetIO extends AgentMessage
{
    @JsonProperty("interfaces")
    private List<String> interfaces = new LinkedList<String>();
    
    public CheckNetIO()
    {
        super();
    }

    public CheckNetIO(AgentMessage message)
    {
        super(message);
    }

    public CheckNetIO(String id)
    {
        super(id);
    }
    
    public CheckNetIO(String... interfaces)
    {
        super();
        for (String name : interfaces)
        {
            this.interfaces.add(name);
        }
    }
    
    public CheckNetIO(Collection<String> interfaces)
    {
        super();
        this.interfaces.addAll(interfaces);
    }

    public List<String> getInterfaces()
    {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces)
    {
        this.interfaces = interfaces;
    }
}
