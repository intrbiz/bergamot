package com.intrbiz.bergamot.agent.server;

import java.util.UUID;

import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;

@FunctionalInterface
public interface AgentKeyResolver
{
    AgentAuthenticationKey resolveKey(UUID keyId);
}
