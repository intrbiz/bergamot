package com.intrbiz.bergamot.model.message.cluster.manager.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.cluster.manager.ClusterManagerResponse;

@JsonTypeName("bergamot.cluster.manager.flushed_global_caches")
public class FlushedGlobalCaches extends ClusterManagerResponse
{    
    public FlushedGlobalCaches()
    {
        super();
    }
}
