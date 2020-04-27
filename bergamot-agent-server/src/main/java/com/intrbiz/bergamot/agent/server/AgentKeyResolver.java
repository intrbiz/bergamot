package com.intrbiz.bergamot.agent.server;

import java.util.UUID;
import java.util.function.Consumer;

import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;

@FunctionalInterface
public interface AgentKeyResolver
{
    void resolveKey(UUID keyId, Consumer<AgentAuthenticationKey> callback);
}
