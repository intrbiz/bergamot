package com.intrbiz.bergamot.compat.config.parser.model;

import java.util.LinkedList;
import java.util.List;

public class ObjectDefinition extends Directive
{
    private final String type;
    
    private final List<ObjectParameter> parameters = new LinkedList<ObjectParameter>();
    
    public ObjectDefinition(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    public List<ObjectParameter> getParameters()
    {
        return parameters;
    }
    
    public void addParameter(ObjectParameter parameter)
    {
        this.parameters.add(parameter);
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        //
        sb.append(this.type).append(" {\r\n");
        for (ObjectParameter parameter : this.parameters)
        {
            sb.append("  ").append(parameter.getName()).append(" => ").append(parameter.getValue()).append(";\r\n");
        }
        sb.append("}\r\n");
        //
        return sb.toString();
    }
}
