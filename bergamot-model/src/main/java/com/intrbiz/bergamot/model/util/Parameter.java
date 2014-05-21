package com.intrbiz.bergamot.model.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.BergamotObject;
import com.intrbiz.bergamot.model.message.ParameterMO;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
@JsonTypeName("bergamot.parameter")
public class Parameter extends BergamotObject<ParameterMO>
{
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("value")
    private String value;
    
    public Parameter()
    {
        super();
    }
    
    public Parameter(String name, String value)
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
    
    public static Parameter parse(String parameter)
    {
        int idx = parameter.indexOf("=");
        if (idx > 0)
        {
            return new Parameter(parameter.substring(0, idx), parameter.substring(idx + 1));
        }
        else
        {
            return new Parameter(parameter, null);
        }
    }
    
    @Override
    public ParameterMO toMO(boolean stub)
    {
        return new ParameterMO(this.getName(), this.getValue());
    }
}
