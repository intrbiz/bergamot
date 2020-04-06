package com.intrbiz.bergamot.cluster.election;

import java.util.UUID;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.intrbiz.bergamot.cluster.util.ZKPaths;

/**
 * Elect a leader amongst the processors which execute specific coordination tasks for the whole cluster
 */
public final class LeaderElector extends GenericElector
{    
    public LeaderElector(ZooKeeper zooKeeper, UUID id) throws KeeperException, InterruptedException
    {
        super(zooKeeper, ZKPaths.POOLS, ZKPaths.LEADER, id);
    }
    
}
