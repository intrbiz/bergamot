package com.intrbiz.bergamot.model.message.cluster.manager.request;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.cluster.manager.ClusterManagerRequest;

/**
 * Flush the global caches of the UI cluster
 */
@JsonTypeName("bergamot.cluster.manager.flush_global_caches")
public class FlushGlobalCaches extends ClusterManagerRequest
{    
    public FlushGlobalCaches()
    {
        super();
    }
}
