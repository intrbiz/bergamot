package com.intrbiz.bergamot.model.message.agent.stat;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.stat.who.WhoInfo;

@JsonTypeName("bergamot.agent.stat.who")
public class WhoStat extends AgentMessage
{
    @JsonProperty("users")
    private List<WhoInfo> users = new LinkedList<WhoInfo>();

    public WhoStat()
    {
        super();
    }

    public WhoStat(AgentMessage message)
    {
        super(message);
    }

    public WhoStat(String id)
    {
        super(id);
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
