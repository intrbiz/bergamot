package com.intrbiz.bergamot.model.message.cluster.manager.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.cluster.manager.ClusterManagerRequest;

/**
 * Stop a site in the UI cluster
 */
@JsonTypeName("bergamot.cluster.manager.deinit_site")
public class DeinitSite extends ClusterManagerRequest
{
    @JsonProperty("site_id")
    private UUID siteId;
    
    @JsonProperty("site_name")
    private String siteName;
    
    public DeinitSite()
    {
        super();
    }
    
    public DeinitSite(UUID siteId, String siteName)
    {
        super();
        this.siteId = siteId;
        this.siteName = siteName;
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public String getSiteName()
    {
        return siteName;
    }

    public void setSiteName(String siteName)
    {
        this.siteName = siteName;
    }
}
