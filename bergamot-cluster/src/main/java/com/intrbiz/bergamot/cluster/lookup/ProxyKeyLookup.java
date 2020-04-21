package com.intrbiz.bergamot.cluster.lookup;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.intrbiz.bergamot.model.ProxyKey;

public interface ProxyKeyLookup
{
    CompletionStage<ProxyKey> lookupProxyKey(UUID keyId);
}
