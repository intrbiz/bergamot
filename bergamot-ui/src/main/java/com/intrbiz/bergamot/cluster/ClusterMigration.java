package com.intrbiz.bergamot.cluster;

import java.io.Serializable;

public interface ClusterMigration extends Serializable
{
    boolean applyMigration(ClusterManager manager) throws Exception;
}
