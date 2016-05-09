package com.intrbiz.bergamot.model.message.agent.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

/**
 * The agent registration request has been completed successfully
 *
 */
@JsonTypeName("bergamot.agent.registration.complete")
public class AgentRegistrationComplete extends AgentRegistrationMessage
{
    @JsonProperty("certificate")
    private String certificate;
    
    public AgentRegistrationComplete()
    {
        super();
    }

    public AgentRegistrationComplete(AgentMessage inResponseTo)
    {
        super(inResponseTo);
    }
    
    public AgentRegistrationComplete(AgentMessage inResponseTo, String certificate)
    {
        super(inResponseTo);
        this.certificate = certificate;
    }

    public String getCertificate()
    {
        return certificate;
    }

    public void setCertificate(String certificate)
    {
        this.certificate = certificate;
    }
}
