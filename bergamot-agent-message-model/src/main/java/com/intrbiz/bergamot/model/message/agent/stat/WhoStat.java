package com.intrbiz.bergamot.model.message.agent.stat;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.agent.stat.who.WhoInfo;

@JsonTypeName("bergamot.agent.stat.who")
public class WhoStat extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("users")
    private List<WhoInfo> users = new LinkedList<WhoInfo>();

    public WhoStat()
    {
        super();
    }

    public WhoStat(Message message)
    {
        super(message);
    }

    public List<WhoInfo> getUsers()
    {
        return users;
    }

    public void setUsers(List<WhoInfo> users)
    {
        this.users = users;
    }
}
