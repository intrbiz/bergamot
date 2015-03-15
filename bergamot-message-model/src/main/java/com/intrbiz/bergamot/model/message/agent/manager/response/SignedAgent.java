package com.intrbiz.bergamot.model.message.agent.manager.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;

@JsonTypeName("bergamot.agent.manager.signed_agent")
public class SignedAgent extends AgentManagerResponse
{
    @JsonProperty("certificate_pem")
    private String certificatePEM;
    
    public SignedAgent()
    {
        super();
    }
    
    public SignedAgent(String certificatePEM)
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
