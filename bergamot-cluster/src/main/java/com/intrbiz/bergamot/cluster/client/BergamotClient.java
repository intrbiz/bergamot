package com.intrbiz.bergamot.cluster.client;

import java.util.UUID;

import com.intrbiz.bergamot.cluster.zookeeper.ZooKeeperManager;

/**
 * A Bergamot client daemon which connects to the cluster and provides services
 */
public interface BergamotClient
{
    UUID getId();
    
    ZooKeeperManager getZooKeeper();

    void close();
}
