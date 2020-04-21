package com.intrbiz.bergamot.cluster.lookup;

import java.util.UUID;

import com.intrbiz.bergamot.model.AgentKey;

public interface AgentKeyLookup
{
    AgentKey lookupAgentKey(UUID keyId);
}
