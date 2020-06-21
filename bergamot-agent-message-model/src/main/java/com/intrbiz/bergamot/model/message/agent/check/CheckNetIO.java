package com.intrbiz.bergamot.model.message.agent.check;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.check.netio")
public class CheckNetIO extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("interfaces")
    private List<String> interfaces = new LinkedList<String>();
    
    public CheckNetIO()
    {
        super();
    }

    public CheckNetIO(Message message)
    {
        super(message);
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
