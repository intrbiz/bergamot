package com.intrbiz.bergamot.model.message.cluster.manager.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.cluster.manager.ClusterManagerResponse;

@JsonTypeName("bergamot.cluster.manager.inited_site")
public class InitedSite extends ClusterManagerResponse
{    
    public InitedSite()
    {
        super();
    }
}
