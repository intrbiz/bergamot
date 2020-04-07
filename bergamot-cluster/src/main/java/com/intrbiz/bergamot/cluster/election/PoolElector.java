package com.intrbiz.bergamot.cluster.election;

import java.util.UUID;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.intrbiz.bergamot.cluster.util.ZKPaths;

/**
 * Elect a leader for a specific processing pool
 */
public final class PoolElector extends GenericElector
{   
    private final int pool;
    
    public PoolElector(ZooKeeper zooKeeper, int pool, UUID id) throws KeeperException, InterruptedException
    {
        super(zooKeeper, ZKPaths.POOLS, String.valueOf(pool), id);
        this.pool = pool;
    }
    
    public int getPool()
    {
        return this.pool;
    }
    
    public String toString()
    {
        return String.valueOf(this.pool);
    }
}
