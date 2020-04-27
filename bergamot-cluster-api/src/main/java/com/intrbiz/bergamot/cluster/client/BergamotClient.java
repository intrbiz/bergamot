package com.intrbiz.bergamot.cluster.client;

import java.util.UUID;

/**
 * A Bergamot client daemon which connects to the cluster and provides services
 */
public interface BergamotClient
{
    UUID getId();

    void close();
}
