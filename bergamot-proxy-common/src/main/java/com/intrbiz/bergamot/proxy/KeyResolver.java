package com.intrbiz.bergamot.proxy;

import java.util.UUID;
import java.util.function.BiConsumer;

import com.intrbiz.bergamot.proxy.model.AuthenticationKey;

@FunctionalInterface
public interface KeyResolver
{
    void resolveKey(UUID keyId, BiConsumer<AuthenticationKey, UUID> callback);
}
