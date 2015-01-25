package com.intrbiz.bergamot.cluster.migration;

import java.io.Serializable;

import com.intrbiz.bergamot.cluster.ClusterManager;

public interface ClusterMigration extends Serializable
{
    boolean applyMigration(ClusterManager manager) throws Exception;
}
