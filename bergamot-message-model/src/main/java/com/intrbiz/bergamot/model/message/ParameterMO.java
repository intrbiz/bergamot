package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.host")
public class ParameterMO extends MessageObject
{
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("value")
    private String value;
    
    public ParameterMO()
    {
        super();
    }
    
    public ParameterMO(String name, String value)
    {
        super();
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
