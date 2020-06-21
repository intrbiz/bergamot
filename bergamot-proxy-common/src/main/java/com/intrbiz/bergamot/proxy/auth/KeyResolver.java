package com.intrbiz.bergamot.proxy.auth;

import java.util.UUID;
import java.util.function.BiConsumer;

import com.intrbiz.bergamot.model.AuthenticationKey;

@FunctionalInterface
public interface KeyResolver
{
    void resolveKey(UUID keyId, BiConsumer<AuthenticationKey, UUID> callback);
}
