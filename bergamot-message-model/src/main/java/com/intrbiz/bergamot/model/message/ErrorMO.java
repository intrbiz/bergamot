package com.intrbiz.bergamot.model.message;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@XmlRootElement(name = "bergamot-error")
@XmlType(name = "bergamot-error")
@JsonTypeName("bergamot.error")
public class ErrorMO extends MessageObject
{
    @JsonProperty("message")
    private String message;
    
    public ErrorMO()
    {
        super();
    }
    
    public ErrorMO(String message)
    {
        super();
        this.message = message;
    }

    @XmlElement(name = "message")
    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
