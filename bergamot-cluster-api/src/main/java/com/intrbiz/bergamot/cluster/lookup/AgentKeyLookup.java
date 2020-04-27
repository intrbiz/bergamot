package com.intrbiz.bergamot.cluster.lookup;

import java.util.UUID;
import java.util.function.Consumer;

import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;

public interface AgentKeyLookup
{
    void lookupAgentKey(UUID keyId, Consumer<AgentAuthenticationKey> callback);
}
