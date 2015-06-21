package com.intrbiz.bergamot.model.message.cluster.manager.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.cluster.manager.ClusterManagerResponse;

@JsonTypeName("bergamot.cluster.manager.inited_site")
public class InitedSite extends ClusterManagerResponse
{
    @JsonProperty("certificate_pem")
    private String certificatePEM;
    
    public InitedSite()
    {
        super();
    }
    
    public InitedSite(String certificatePEM)
    {
        this.certificatePEM = certificatePEM;
    }

    public String getCertificatePEM()
    {
        return certificatePEM;
    }

    public void setCertificatePEM(String certificatePEM)
    {
        this.certificatePEM = certificatePEM;
    }
}
