package com.intrbiz.bergamot.model.message.agent.manager.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;

@JsonTypeName("bergamot.agent.manager.signed_server")
public class SignedServer extends AgentManagerResponse
{
    @JsonProperty("certificate_pem")
    private String certificatePEM;
    
    public SignedServer()
    {
        super();
    }
    
    public SignedServer(String certificatePEM)
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
