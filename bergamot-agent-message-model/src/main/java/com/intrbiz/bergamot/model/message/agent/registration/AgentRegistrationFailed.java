package com.intrbiz.bergamot.model.message.agent.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

/**
 * The agent registration request has failed
 *
 */
@JsonTypeName("bergamot.agent.registration.failed")
public class AgentRegistrationFailed extends AgentRegistrationMessage
{
    public enum ErrorCode
    {
        NOT_SUPPORTED,
        BAD_CSR,
        BAD_TEMPLATE,
        NOT_AVAILABLE,
        GENERAL
    }
    
    @JsonProperty("error_code")
    private ErrorCode errorCode;
    
    @JsonProperty("message")
    private String message;
    
    public AgentRegistrationFailed()
    {
        super();
    }

    public AgentRegistrationFailed(AgentMessage inResponseTo)
    {
        super(inResponseTo);
    }
    
    public AgentRegistrationFailed(AgentMessage inResponseTo, ErrorCode errorCode, String message)
    {
        super(inResponseTo);
        this.errorCode = errorCode;
        this.message = message;
    }

    public ErrorCode getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode)
    {
        this.errorCode = errorCode;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
