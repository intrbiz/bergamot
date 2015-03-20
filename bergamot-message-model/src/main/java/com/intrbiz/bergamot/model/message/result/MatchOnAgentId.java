package com.intrbiz.bergamot.model.message.result;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Match a host on Agent Id
 */
@JsonTypeName("bergamot.result.match_on.agent_id")
public class MatchOnAgentId extends MatchOn
{
    @JsonProperty("check_type")
    private UUID agentId;
    
    public MatchOnAgentId()
    {
        super();
    }
    
    public MatchOnAgentId(UUID agentId)
    {
        this.agentId = agentId;
    }

    public UUID getAgentId()
    {
        return agentId;
    }

    public void setAgentId(UUID agentId)
    {
        this.agentId = agentId;
    }
}
