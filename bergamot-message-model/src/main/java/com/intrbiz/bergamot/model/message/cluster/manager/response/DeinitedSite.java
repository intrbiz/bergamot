package com.intrbiz.bergamot.model.message.cluster.manager.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.cluster.manager.ClusterManagerResponse;

@JsonTypeName("bergamot.cluster.manager.deinited_site")
public class DeinitedSite extends ClusterManagerResponse
{    
    public DeinitedSite()
    {
        super();
    }
}
