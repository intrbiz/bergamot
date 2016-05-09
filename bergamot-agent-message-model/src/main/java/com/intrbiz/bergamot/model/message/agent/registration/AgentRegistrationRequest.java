package com.intrbiz.bergamot.model.message.agent.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Request this agent to be registered using the template as identified by the 
 * client certificate and with the host name provided.
 *
 */
@JsonTypeName("bergamot.agent.registration.request")
public class AgentRegistrationRequest extends AgentRegistrationMessage
{
    @JsonProperty("certificate_request")
    private String certificateRequest;
    
    public AgentRegistrationRequest()
    {
        super();
    }

    public AgentRegistrationRequest(String id)
    {
        super(id);
    }

    public String getCertificateRequest()
    {
        return certificateRequest;
    }

    public void setCertificateRequest(String certificateRequest)
    {
        this.certificateRequest = certificateRequest;
    }
}
