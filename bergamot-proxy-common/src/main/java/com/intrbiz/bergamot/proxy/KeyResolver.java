package com.intrbiz.bergamot.proxy;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.intrbiz.bergamot.proxy.model.AuthenticationKey;

@FunctionalInterface
public interface KeyResolver
{
    CompletionStage<AuthenticationKey> resolveKey(UUID keyId);
}
